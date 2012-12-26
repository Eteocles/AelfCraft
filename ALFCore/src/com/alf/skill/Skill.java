package com.alf.skill;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.server.v1_4_5.DamageSource;
import net.minecraft.server.v1_4_5.EntityBlaze;
import net.minecraft.server.v1_4_5.EntityEnderman;
import net.minecraft.server.v1_4_5.EntityGiantZombie;
import net.minecraft.server.v1_4_5.EntityGolem;
import net.minecraft.server.v1_4_5.EntityLiving;
import net.minecraft.server.v1_4_5.EntityMonster;
import net.minecraft.server.v1_4_5.EntityPlayer;
import net.minecraft.server.v1_4_5.EntitySilverfish;
import net.minecraft.server.v1_4_5.EntitySpider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_4_5.event.CraftEventFactory;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;
import com.alf.util.Setting;

/**
 * Abstract spell type which can be invoked by a player.
 * Handling the damageEntity and knockback methods is annoying, due to obfuscation changes
 * from  version to version changes.
 * Use MinecraftCoderPack decompiler to identify changes in variable types.
 * @author Eteocles
 */
public abstract class Skill extends BasicCommand {

	public final AlfCore plugin;
	private Configuration defaultConfig = new MemoryConfiguration();
	private static Random random = new Random();
	private final Set<SkillType> types = EnumSet.noneOf(SkillType.class);
	private static Field ldbpt;
	
	public Skill(AlfCore plugin, String name) {
		super(name);
		this.plugin = plugin;
	}
	
	/**
	 * Description of the Skill.
	 * @param a
	 * @return
	 */
	public abstract String getDescription(Alf a);
	
	/**
	 * Execute the Skill's mechanics.
	 */
	public abstract boolean execute(CommandSender cs, String command, String[] args);
	
	/**
	 * Initiate the Skill.
	 */
	public abstract void init();

	/**
	 * Add the entity as a target of the alf by this spell.
	 * @param o
	 * @param alf
	 */
	public void addSpellTarget(Entity o, Alf alf) {
		this.plugin.getDamageManager().addSpellTarget(o, alf, this);
	}
	
	/**
	 * Broadcast spell information to nearby players from the caster.
	 * @param source
	 * @param message
	 * @param args
	 */
	public void broadcast(Location source, String message, Object[] args) {
		if (! (message == null || message.isEmpty() || message.equalsIgnoreCase("off"))) {
			Player players[] = this.plugin.getServer().getOnlinePlayers();
			for (Player p : players) {
				Location pLocation = p.getLocation();
				Alf alf = this.plugin.getCharacterManager().getAlf(p);
				if (! alf.isSuppressing(this)) {
					if (source.getWorld().equals(pLocation.getWorld()) &&
							isInMsgRange(pLocation, source))
						Messaging.send(p, message, args);
				}
			}
		}
	}

	/**
	 * Call a damage event and check if a player has damaged a target.
	 * Actual quantity of damage is 0.
	 * @param applier
	 * @param player
	 * @return
	 */
	public static boolean damageCheck(Player player, LivingEntity target) {
		if (! player.equals(target)) {
			EntityDamageByEntityEvent damageEntityEvent = new EntityDamageByEntityEvent(
					player, target, EntityDamageEvent.DamageCause.CUSTOM, 0);
			Bukkit.getServer().getPluginManager().callEvent(damageEntityEvent);
			if (damageEntityEvent.isCancelled())
				return false;
			damageEntityEvent = new EntityDamageByEntityEvent(player, target,
					EntityDamageEvent.DamageCause.CUSTOM, 0);
			Bukkit.getServer().getPluginManager().callEvent(damageEntityEvent);
			if (damageEntityEvent.isCancelled())
				return false;
			return true;
		}
		return false;
	}
	
	/**
	 * Acquire the default config for this Skill.
	 * @return
	 */
	public ConfigurationSection getDefaultConfig() {
		return this.defaultConfig;
	}
	
	/** Return the set of Skill Types defined for this particular Skill. */
	public Set<SkillType> getTypes()
	{	return Collections.unmodifiableSet(this.types);	}

	/** Determines whether or not this Skill is listed as a /help Command. */
	public boolean isShownOnHelpMenu()
	{	return false;	}

	/** Checks whether or not this Skill fits a type. */
	public boolean isType(SkillType type)
	{	return this.types.contains(type);	}

	/** Defines the type set for the Skill. */
	protected void setTypes(SkillType[] type)
	{	this.types.addAll(types);	}

	
	/**
	 * Knock back the targeted entity by a certain amount given the quantity of damage.
	 * @param target
	 * @param attacker
	 * @param damage
	 */
	public static void knockBack(LivingEntity target, LivingEntity attacker, int damage) {
		EntityLiving el = ((CraftLivingEntity)target).getHandle();
		EntityLiving aEL = ((CraftLivingEntity)attacker).getHandle();
		el.velocityChanged = true;
		double d0 = aEL.locX - el.locX, d1;
		for (d1 = aEL.locZ - el.locZ; d0 * d0 + d1 * d1 < 0.0001D; d1 = (Math.random() - Math.random()) * 0.01D)
			d0 = (Math.random() - Math.random()) * 0.01D;
		//el.deathTime flag. Change from aW to aX in 1.4.6
		el.aW = ((float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - el.yaw);
		//public void knockBack(Entity, int, double, double)
		el.a(aEL, damage, d0, d1);
	}
	
	/**
	 * 
	 * @param target
	 * @param attacker
	 * @param damage
	 * @param cause
	 * @param knockback
	 * @return
	 */
	public static boolean damageEntity(LivingEntity target, LivingEntity attacker, int damage, 
			EntityDamageEvent.DamageCause cause, boolean knockback) {
		if (! target.isDead() && target.getHealth() > 0) {
			//Number of damage ticks.
			((CraftLivingEntity) target).setNoDamageTicks(0);
			//Call a damage event.
			EntityDamageByEntityEvent edbe = new EntityDamageByEntityEvent(attacker, target, cause, damage);
			Bukkit.getServer().getPluginManager().callEvent(edbe);
			if (edbe.isCancelled())
				return false;
			target.setLastDamageCause(edbe);
			int oldHealth = target.getHealth(), newHealth = oldHealth - edbe.getDamage();
			//Handle negative health.
			if (newHealth < 0)
				newHealth = 0;
			//Alter the handle.
			EntityLiving el = ((CraftLivingEntity) target).getHandle();
			el.lastDamage = edbe.getDamage();
			//hurtTicks (maxHurtTime) = attackedAtYaw (aV -> aW) = 10
			//Change from aV to aW in 1.4.6
			el.hurtTicks = el.aV = 10;
			//el.deathTime flag. Change from aW to aX in 1.4.6
			el.aW = 0.0F;
			if (knockback)
				knockBack(target, attacker, edbe.getDamage());
			//TODO Look into this later.
			el.world.broadcastEntityEffect(el, (byte) 2);
			//Last damaging entity.
			el.lastDamager = ((CraftLivingEntity) attacker).getHandle();
			if (attacker instanceof Player)
				try {
					ldbpt.set(el, 60);
				} catch (IllegalArgumentException e) {} catch (IllegalAccessException e) {}
			//Update health.
			el.setHealth(newHealth);
			//Handle death.
			if (newHealth <= 0) {
				el.world.makeSound(el, getSoundName(target.getType()), 1.0F, getSoundStrength(el));
				if (attacker instanceof Player) {
					EntityPlayer p = ((CraftPlayer) attacker).getHandle();
					//Set the entity's killer as the player.
					el.killer = p;
					//public void setRevengeTarget(EntityLiving) 
					//Set the revenge target as the player.
					el.c(p);
					el.die(DamageSource.playerAttack(p));
				} else {
					EntityLiving att = ((CraftLivingEntity) attacker).getHandle();
					//Set the revenge target as the entity.
					el.c(att);
					el.die(DamageSource.mobAttack(att));
				}
			} else {
				//If not dead...
				((CraftLivingEntity) target).setNoDamageTicks(0);
				EntityLiving attackEntity = ((CraftLivingEntity) attacker).getHandle();
				if (el instanceof EntityMonster) {
					if (el instanceof EntityBlaze || el instanceof EntityEnderman ||
							el instanceof EntitySpider || el instanceof EntityGiantZombie ||
							el instanceof EntitySilverfish) {
						EntityMonster em = (EntityMonster) el;
						EntityTargetEvent ev = CraftEventFactory.callEntityTargetEvent(em, 
								attackEntity, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY);
						if (! ev.isCancelled())
							em.setTarget(attackEntity);
					}
				}
				else if (el instanceof EntityGolem) {
					EntityGolem eg = (EntityGolem) el;
					eg.setTarget(((CraftLivingEntity) attacker).getHandle());
				} else if (target instanceof Wolf && ((Wolf)target).getTarget() == null) {
					Wolf wolf = (Wolf) target;
					wolf.setAngry(true);
					wolf.setTarget(attacker);
				} else if (target instanceof PigZombie)
					((PigZombie)target).setAngry(true);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Determines whether or not the given player has sufficient item(s) as reagents.
	 * @param player - player in question
	 * @param itemStack - reagents being checked
	 * @return if the conditions are fulfilled
	 */
	protected boolean hasReagentCost(Player player, ItemStack itemStack) {
		if (itemStack == null || itemStack.getAmount() == 0)
			return true;
		int amount = 0;
		for (ItemStack stack : player.getInventory().all(itemStack.getType()).values()) {
			amount += stack.getAmount();
			if (amount >= itemStack.getAmount())
				return true;
		} return false;
	}
	
	/**
	 * Extract the ItemStack for the reagent of this skill from a given player. 
	 * @param hero - hero to be checked
	 * @return the itemStack or null
	 */
	protected ItemStack getReagentCost(Alf alf) {
		int reagentCost = SkillConfigManager.getUseSetting(alf, this, Setting.REAGENT_COST, 0, true);
		String reagentName = SkillConfigManager.getUseSetting(alf, this, Setting.REAGENT, (String)null);
		ItemStack itemStack = null;
		if (reagentCost > 0 && reagentName != null && reagentName != "") {
			//Separate item id from data value.
			String vals[] = reagentName.split(":");
			try {
				int id = Integer.parseInt(vals[0]);
				byte sub = 0;
				if (vals.length > 1)
					sub = (byte)Integer.parseInt(vals[1]);
				itemStack = new ItemStack(id, reagentCost, (short)sub);
			} catch (NumberFormatException e) {
				AlfCore.log(Level.SEVERE, "Invalid skill reagent defined in " + getName() +
						". Please switch to new format ID:DATA.");
			}
		}
		return itemStack;
	}
	
	/**
	 * Defines comparison for Skills.
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj.toString().equals(getName()))
			return true;
		if (!(obj instanceof Skill))
			return false;
		Skill s = (Skill)obj;
		return s.getName().equals(getName());
	}
	
	/** Use the String's Hash Code for Convenience. */ 
	public int hashCode()
	{	return getName().hashCode();	}
	
	/**
	 * Get the strength of the sound of a living entity.
	 * @param el
	 * @return
	 */
	private static float getSoundStrength(EntityLiving el) {
		return el.isBaby() ? 
				(random.nextFloat() - random.nextFloat()) * 0.2F + 1.5F : 
					(random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
	}
	
	/**
	 * Whether the locations are in message range.
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	private boolean isInMsgRange(Location loc1, Location loc2) {	
		return (Math.abs(loc1.getBlockX() - loc2.getBlockX()) < 25) && 
				(Math.abs(loc1.getBlockY() - loc2.getBlockY()) < 25) && 
				(Math.abs(loc1.getBlockZ() - loc2.getBlockZ()) < 25);	
		}

	@SuppressWarnings("incomplete-switch")
	/**
	 * Get the sound name for a given type on death.
	 * https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/org/bukkit/craftbukkit/CraftSound.java
	 * @param type
	 * @return
	 */
	private static String getSoundName(EntityType type) {
		switch (type) {
		case BLAZE:
			return "mob.blaze.death";
		case CHICKEN:
			return "mob.chicken.hurt";
		case CREEPER:
			return "mob.creeper.death";
		case SLIME:
		case MAGMA_CUBE:
			return "mob.slime";
		case SKELETON:
			return "mob.skeletonhurt";
		case IRON_GOLEM:
			return "mob.irongolem.death";
		case GHAST:
			return "mob.ghast.death";
		case PIG:
			return "mob.pig.death";
		case OCELOT:
			return "mob.cat.hitt";
		case SHEEP:
			return "mob.sheep";
		case CAVE_SPIDER:
		case SPIDER:
			return "mob.spiderdeath";
		case WOLF:
			return "mob.wolf.death";
		case GIANT:
		case ZOMBIE:
			return "mob.zombie.death";
		case BAT:
			return "mob.bat";
		case MUSHROOM_COW:
		case COW:
			return "mob.cow.hurt";
		case ENDERMAN:
			return "mob.endermen.death";
		case PIG_ZOMBIE:
			return "mob.zombiepig.zpigdeath";
		case SILVERFISH:
			return "mob.silverfish";
		case WITCH:
			return "mob.witch";
		}
		return "damage.hurtflesh";
	}
	
	static {
		try {
			ldbpt = EntityLiving.class.getDeclaredField("lastDamageByPlayerTime");
			ldbpt.setAccessible(true);
		}
		catch (SecurityException e) {}
		catch (NoSuchFieldException e) {}
	}

}
