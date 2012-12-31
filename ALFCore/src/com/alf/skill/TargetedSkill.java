package com.alf.skill;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.effect.EffectType;
import com.alf.util.Messaging;
import com.alf.util.Setting;
import com.alf.util.Util;

/**
 * Describes a targeted skill.
 * @author Eteocles
 */
public abstract class TargetedSkill extends ActiveSkill {

	/**
	 * Construct the targeted skill.
	 * @param plugin
	 * @param name
	 */
	public TargetedSkill(AlfCore plugin, String name) {
		super(plugin, name);
	}
	
	  public ConfigurationSection getDefaultConfig()
	  {
	    ConfigurationSection section = super.getDefaultConfig();
	    section.set(Setting.USE_TEXT.node(), "%alf% used %skill% on %target%!");
	    section.set(Setting.MAX_DISTANCE.node(), Integer.valueOf(15));
	    return section;
	  }

	/**
	 * Initiate the targeted skill.
	 */
	public void init() {
		String useText = SkillConfigManager.getRaw(this, Setting.USE_TEXT.node(),
				"%alf% used %skill% on %target%!");
		useText = useText.replace("%alf%",  "$1").replace("%skill%", "$2").replace("%target%", "$3");
		setUseText(useText);
	}

	/**
	 * Broadcast that an Alf has used a targeted skill.
	 * @param alf
	 * @param target
	 */
	protected void broadcastExecuteText(Alf alf, LivingEntity target) {
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), getUseText(), 
				new Object[] {
			player.getDisplayName(), getName(), target == player ? "themself" : getEntityName(target)
		});
	}

	/**
	 * Get the given entity's name.
	 * @param entity
	 * @return
	 */
	public static String getEntityName(LivingEntity entity) {
		return (entity instanceof Player) ? ((Player)entity).getName() :
			entity.getClass().getSimpleName().substring(5);
	}

	/**
	 * Upon invocation of this Skill, determine the targeted entity.
	 * @param alf
	 * @param maxDistance
	 * @param args
	 * @return
	 */
	public LivingEntity getTarget(Alf alf, int maxDistance, String[] args) {
		Player player = alf.getPlayer();
		LivingEntity target = null;
		if (args.length > 0) {
			target = this.plugin.getServer().getPlayer(args[0]);
			//Player target.
			if (target == null) {
				Messaging.send(player, "Invalid target!", new Object[0]);
				return null;
			}
			//Target is not within the same world.
			if (! target.getLocation().getWorld().equals(player.getLocation().getWorld())) {
				Messaging.send(player, "Target is in a different dimension.", new Object[0]);
				return null;
			}
			//Target is too far.
			int distSq = maxDistance * maxDistance;
			if (target.getLocation().distanceSquared(player.getLocation()) > distSq) {
				Messaging.send(player, "Target is too far away.", new Object[0]);
				return null;
			}
			//Target is not visible.
			if (! inLineOfSight(player, (Player) target)) {
				Messaging.send(player, "Sorry,  target is not in your line of sight!", new Object[0]);
				return null;
			}
			//Target is dead.
			if (target.isDead() || target.getHealth() == 0) {
				Messaging.send(player, "You can't target the forsaken dead!", new Object[0]);
				return null;
			}
		}

		if (target == null) {
			target = getPlayerTarget(player, maxDistance);
			if (isType(SkillType.HEAL)){
				if (target instanceof Player && alf.hasParty() && alf.getParty().isPartyMember((Player) target))
					return target;
				if (target instanceof Player)
					return null;
				target = null;
			}
		}

		if (target == null) {
			if (isType(SkillType.HARMFUL))
				return null;
			target = player;
		}

		if (isType(SkillType.HARMFUL) && (player.equals(target) || 
				alf.getSummons().contains(target) || ! damageCheck(player, target))) {
			Messaging.send(player, "Sorry,  you can't damage that target!", new Object[0]);
			return null;
		}
		return target;
	}

	/**
	 * Get a player's target.
	 * @param player
	 * @param maxDistance
	 * @return
	 */
	public static LivingEntity getPlayerTarget(Player player, int maxDistance) {
		if (player.getLocation().getBlockY() > player.getWorld().getMaxHeight())
			return null;
		List<Block> lineOfSight = player.getLineOfSight(Util.transparentIds, maxDistance);
		Set<Location> locs = new HashSet<Location>();
		for (Block block : lineOfSight) {
			locs.add(block.getRelative(BlockFace.UP).getLocation());
			locs.add(block.getLocation());
			locs.add(block.getRelative(BlockFace.DOWN).getLocation());
		}
		lineOfSight = null;
		List<Entity> nearbyEntities = player.getNearbyEntities(maxDistance, maxDistance, maxDistance);
		for (Entity entity : nearbyEntities) {
			//Target must be alive, nearby, and visible.
			if (entity instanceof LivingEntity && ! entity.isDead() && 
					((LivingEntity)entity).getHealth() != 0 &&
					locs.contains(entity.getLocation().getBlock().getLocation())) {
				if (! (entity instanceof Player) || player.canSee((Player) entity))
					return (LivingEntity) entity;
			}
		}

		return null;
	}

	/**
	 * Set the delayed targeting skill for the given Alf.
	 * Takes a player's target and begins the use of a delayed targeting skill.
	 */
	protected boolean addDelayedSkill(Alf alf, int delay, String identifier, String[] args) {
		Player player = alf.getPlayer();
		int maxDistance = SkillConfigManager.getUseSetting(alf, this, Setting.MAX_DISTANCE, 15, false);
		LivingEntity target = getTarget(alf, maxDistance, args);

		if (target == null)
			return false;
		if (args.length > 1 && target != null)
			args = (String[]) Arrays.copyOfRange(args, 1, args.length);

		DelayedSkill dSkill = new DelayedTargetedSkill(identifier, player, delay, this, target, args);

		broadcast(player.getLocation(), "$1 begins to use $2 on $3!", new Object[] { 
			player.getDisplayName(), getName(), Messaging.getLivingEntityName(target)
		});

		this.plugin.getCharacterManager().getDelayedSkills().put(alf, dSkill);
		alf.setDelayedSkill(dSkill);
		return true;
	}

	/**
	 * Whether a player is in the line of sight of another player.
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean inLineOfSight(Player a, Player b) {
		if (a == b || a.equals(b))
			return true;
		Location aLoc = a.getEyeLocation();
		Location bLoc = b.getEyeLocation();

		int distance = (int) aLoc.distance(bLoc);
		if (distance > 120)
			return false;

		Vector ab = new Vector(bLoc.getX() - aLoc.getX(), bLoc.getY() - aLoc.getY(), 
				bLoc.getZ() - aLoc.getZ());

		try {
			Iterator<Block> iterator = new BlockIterator(a.getWorld(), aLoc.toVector(), ab, 0.0D, 
					distance + 1);
			while (iterator.hasNext()) {
				Block block = iterator.next();
				Material type = block.getType();
				//If block in the way is opaque...
				if (! Util.transparentBlocks.contains(type))
					return false;
			}
		} catch (Exception e) {
			AlfCore.log(Level.WARNING, "Error in Bukkit attempting to create/find line of sight of a player.");
			return false;
		}
		return true;
	}

	/**
	 * Use the TargetedSkill.
	 * @param alf
	 * @param target
	 * @param args
	 * @return
	 */
	public abstract SkillResult use(Alf alf, LivingEntity target, String[] args);

	/**
	 * The given Alf uses the skill.
	 */
	public SkillResult use(Alf alf, String[] args) {
		int maxDistance = SkillConfigManager.getUseSetting(alf, this, Setting.MAX_DISTANCE, 15, false);
		double distBonus = SkillConfigManager.getUseSetting(alf, this, Setting.MAX_DISTANCE_INCREASE, 
				0.0D, false)*alf.getSkillLevel(this);
		maxDistance += (int) distBonus;
		if (alf.hasEffectType(EffectType.BLIND)) {
			Messaging.send(alf.getPlayer(), "You can't target anything while blinded!", new Object[0]);
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		LivingEntity target = getTarget(alf, maxDistance, args);

		if (target == null)
			return SkillResult.INVALID_TARGET_NO_MSG;

		if (args.length > 1 && target != null)
			args = (String[]) Arrays.copyOfRange(args, 1, args.length);

		SkillResult result = use(alf, target, args);

		if (isType(SkillType.INTERRUPT) && result.equals(SkillResult.NORMAL) && target instanceof
				Player) {
			Alf tAlf = this.plugin.getCharacterManager().getAlf((Player) target);
			if (tAlf.getDelayedSkill() != null) {
				tAlf.cancelDelayedSkill();
				tAlf.setCooldown("global", AlfCore.properties.globalCooldown);
			}
		}

		return result;
	}

	/**
	 * The given Alf uses this delayed tpe skill.
	 * @param alf
	 * @param target
	 * @param args
	 * @return
	 */
	public SkillResult useDelayed(Alf alf, LivingEntity target, String[] args) {
		Player player = alf.getPlayer();
		if (alf.hasEffectType(EffectType.BLIND)) {
			Messaging.send(alf.getPlayer(), "You can't target anything while blinded!", new Object[0]);
			return SkillResult.INVALID_TARGET_NO_MSG;
		}

		int maxDistance = SkillConfigManager.getUseSetting(alf, this, Setting.MAX_DISTANCE, 15, false);
		maxDistance *= maxDistance;

		if (!player.getWorld().equals(target.getWorld()) || 
				player.getLocation().distanceSquared(target.getLocation()) > maxDistance) {
			Messaging.send(player, "Target is out of range!", new Object[0]);
			return SkillResult.FAIL;
		}

		SkillResult result = use(alf, target, args);

		if (isType(SkillType.INTERRUPT) && result.equals(SkillResult.NORMAL) &&
				target instanceof Player) {
			Alf tAlf = this.plugin.getCharacterManager().getAlf((Player) target);
			if (tAlf.getDelayedSkill() != null) {
				tAlf.cancelDelayedSkill();
				tAlf.setCooldown("global", AlfCore.properties.globalCooldown + System.currentTimeMillis());
			}
		}
		return result;
	}

}
