package com.alf.skill.skills;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.character.Pet;
import com.alf.character.effect.common.StunEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.FireworkEffectUtil;
import com.alf.util.Messaging;
import com.alf.util.Setting;
import com.alf.util.Util;

/**
 * A charging skill that stuns or applies certain effects to a player/entity when landed near.
 */
public class SkillStarBurst extends ActiveSkill {

	private Set<String> usingPlayers = new HashSet<String>();

	public SkillStarBurst(AlfCore plugin) {
		super(plugin, "StarBurst");
		setDescription("You dash forward with a brilliant flash of coloured light, damaging the first enemy you hit!");
		setUsage("/skill starburst");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill starburst" });
		setTypes(new SkillType[] {SkillType.DAMAGING, SkillType.MOVEMENT, SkillType.PHYSICAL});
		setUseText("%alf% uses %skill%");
		Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
		setFireworkEffect(FireworkEffect.builder().flicker(true).withColor(Color.RED).withColor(Color.MAROON).trail(true).with(Type.BURST).build());
	}

	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection section = super.getDefaultConfig();
		section.set("stun-duration", 1500);
		section.set("slow-duration", 0);
		section.set("root-duration", 0);
		section.set("silence-duration", 0);
		section.set(Setting.DAMAGE.node(), 0);
		section.set(Setting.DAMAGE_INCREASE.node(), 1000);
		section.set(Setting.RADIUS.node(), 2);
		section.set("charge-velocity", 3);
		return section;
	}

	/**
	 * Charge the player and add delayed check for skill end.
	 */
	public SkillResult use(Alf alf, String[] args)
	{
		final Player player = alf.getPlayer();

		player.setFireTicks(0);

		double yaw = player.getEyeLocation().getYaw();
		double pitch = player.getEyeLocation().getPitch();
		double[] vector = Util.toCartesian(yaw, pitch, 3);
		Vector v = new Vector(vector[0], 0, vector[2]);

		if (Util.isWayBlocked(player.getWorld(), player.getEyeLocation(), player.getEyeLocation().add(v), 0, 4) || 
				Util.isWayBlocked(player.getWorld(), player.getLocation(), player.getLocation().add(v), 0, 4)) {
			Messaging.send(player, "You can not use that skill as the way is blocked!", new Object[0]);
			return SkillResult.FAIL;
		}

		//Display firework effect :)
		try {
			FireworkEffectUtil.playFirework(player.getWorld(), player.getLocation(), getFireworkEffect());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		player.setVelocity(v);

		this.usingPlayers.add(alf.getName());
		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
		{
			public void run() {
				player.setFallDistance(8.0F);
			}
		}
		, 2L);

		broadcastExecuteText(alf);
		return SkillResult.NORMAL;
	}

	/**
	 * Get the description of the skill.
	 */
	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}

	/**
	 * Listen to the skill damage.
	 * @author Eteocles
	 */
	public class SkillEntityListener implements Listener {
		private final Skill skill;

		public SkillEntityListener(Skill skill) {
			this.skill = skill;
		}

		/**
		 * On entity fall damage, pop the player from the queue and handle damaging.
		 * @param event
		 */
		@EventHandler(priority=EventPriority.LOWEST)
		public void onEntityDamage(EntityDamageEvent event) {
			if (!event.getCause().equals(DamageCause.FALL) || !(event.getEntity() instanceof Player) 
					|| !SkillStarBurst.this.usingPlayers.contains(((Player)event.getEntity()).getName())) {
				return;
			}
			Player player = (Player)event.getEntity();
			Alf alf = SkillStarBurst.this.plugin.getCharacterManager().getAlf(player);
			SkillStarBurst.this.usingPlayers.remove(alf.getName());
			AlfCore.log(Level.INFO, "Player landed.");
			//Don't actually deal fall damage.
			event.setDamage(0);
			event.setCancelled(true);

			//Get settings.
			int radius = SkillConfigManager.getUseSetting(alf, this.skill, Setting.RADIUS.node(), 2, false);
			int damage = SkillConfigManager.getUseSetting(alf, this.skill, Setting.DAMAGE.node(), 0, false);
			int stunDuration = SkillConfigManager.getUseSetting(alf, this.skill, "stun-duration", 1500, false);
			damage += SkillConfigManager.getUseSetting(alf, this.skill, Setting.DAMAGE_INCREASE, 0, false) * alf.getSkillLevel(this.skill);

			int affectedEntities = 0;
			
			for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
				if (e instanceof LivingEntity) {
					LivingEntity le = (LivingEntity) e;
					if (Skill.damageCheck(player, le)) {
						if (e instanceof Player) {
							Player p = (Player)e;
							Alf tAlf = SkillStarBurst.this.plugin.getCharacterManager().getAlf(p);
							if (stunDuration > 0L)
				                tAlf.addEffect(new StunEffect(this.skill, stunDuration));
							if (damage > 0) {
								SkillStarBurst.this.addSpellTarget(le, alf);
								Skill.damageEntity(le, player, damage, DamageCause.ENTITY_ATTACK, false);
							}
							affectedEntities++;
						} else {
							CharacterTemplate monster = SkillStarBurst.this.plugin.getCharacterManager().getCharacter((le));
							if (monster instanceof Pet) {
								return;
							}
						}
						if (damage > 0)
							Skill.damageEntity(le, player, damage, EntityDamageEvent.DamageCause.ENTITY_ATTACK, true);
						affectedEntities++;
					}
				}
			}
			
			if (affectedEntities > 0)
				Messaging.send(player, "Your StarBurst hit $1 enemies!", new Object[] { affectedEntities-1 });
		}
	}

}
