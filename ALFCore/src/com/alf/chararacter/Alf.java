package com.alf.chararacter;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;

import com.alf.AlfCore;
import com.alf.api.AlfDamageCause;
import com.alf.chararacter.classes.AlfClass;
import com.alf.chararacter.effect.CombatEffect;
import com.alf.chararacter.party.AlfParty;
import com.alf.skill.DelayedSkill;
import com.alf.skill.Skill;
import com.alf.util.Properties;
import com.alf.util.Setting;

/**
 * Encapsulates a player type.
 * Stores most player specific information.
 * @author Eteocles
 */
public class Alf extends CharacterTemplate {

	public static final DecimalFormat decFormat = new DecimalFormat("#0.##");
	private Player player;
	//Class specifications.
	private AlfClass alfClass;
	private AlfClass secondClass;
	//Mana
	private AtomicInteger mana = new AtomicInteger(0);
	//Player Party
	private AlfParty party = null;
	//Output messages or not
	private AtomicBoolean verbose = new AtomicBoolean(true);
	//Last damage cause for this Alf.
	private AlfDamageCause lastDamageCause = null;
	//Types of experience.
	private Map<String, Double> experience = new ConcurrentHashMap<String, Double>();
	private Map<Material, String[]> binds = new ConcurrentHashMap<Material, String[]>();
	private Map<String, ConfigurationSection> skills = new HashMap<String, ConfigurationSection>();
	//Permission values.
	private PermissionAttachment transientPerms;
	//Combat effect.
	private final CombatEffect combat;
	//
	ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	
	
	/**
	 * Construct an Alf.
	 * @param plugin
	 * @param lEntity
	 * @param name
	 */
	public Alf(AlfCore plugin, Player player, AlfClass alfClass, AlfClass secondClass) {
		super(plugin, player, player.getName());
		this.player = player;
		this.alfClass = alfClass;
		this.secondClass = secondClass;
		
		this.combat = new CombatEffect(plugin);
//		addEffect(this.combat);
		this.transientPerms = player.addAttachment(plugin);
	}
	
	/**
	 * Add the permission.
	 * @param permission
	 */
	public void addPermission(String permission) {
		this.transientPerms.setPermission(permission, true);
	}
	
	/**
	 * Add the permission.
	 * @param permission
	 */
	public void addPermission(Permission permission) {
		this.transientPerms.setPermission(permission, true);
	}
	
	/**
	 * Add a skill to this Alf.
	 * @param skill
	 * @param section
	 */
	public void addSkill(String skill, ConfigurationSection section) {
		this.skills.put(skill.toLowerCase(), section);
	}

	/**
	 * Whether this Alf has an experience type.
	 * @param type
	 * @return
	 */
	public boolean hasExperienceType(AlfClass.ExperienceType type) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Whether this Alf can gain an experience type.
	 * @param type
	 * @return
	 */
	public boolean canGain(AlfClass.ExperienceType type) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Bind a skill to a material.
	 * @param material
	 * @param skillName
	 */
	public void bind(Material material, String[] skillName) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Clear all binds for this Alf.
	 */
	public void clearBinds() {
		this.binds.clear();
	}
	
	/**
	 * Clear all cooldowns for this Alf.
	 */
	public void clearCooldowns() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Clear experience for this Alf.
	 */
	public void clearExperience() {
		for (Map.Entry<String, Double> entry : this.experience.entrySet()) 
			entry.setValue(0.0D);
	}
	
	/**
	 * Clear summons for this Alf.
	 */
	public void clearSummons() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Whether an Alf is equivalent to an object type.
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		Alf other = (Alf) obj;
		if (this.player == null) {
			if (other.player != null)
				return false;
		}
		else if (! this.name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * Add experience to a specific AlfClass type from a location.
	 * @param expChange
	 * @param ac
	 * @param loc
	 */
	public void addExp(double expChange, AlfClass ac, Location loc) {
		throw new Error("Implement me!");
	}
	
	/**
	 * The Alf gains Exp through a specific means.
	 * @param expChange
	 * @param source
	 * @param loc
	 */
	public void gainExp(double expChange, AlfClass.ExperienceType source, Location loc) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the amount of exp needed to get to the next level for the alf class.
	 * @param ac
	 * @return
	 */
	public double currentEXPToNextLevel(AlfClass ac) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the amount of exp that should be lost, given the proximity to a level and the amount of experience.
	 * @param multiplier
	 * @param ac
	 * @return
	 */
	protected double calculateEXPLoss(double multiplier, AlfClass ac) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Have a player lose experience from death.
	 * @param multiplier - multiplier for loss
	 * @param pvp - whether the death was from pvp
	 */
	public void loseExpFromDeath(double multiplier, boolean pvp) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the Alf's name.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the bind attributed to a material.
	 * @param mat
	 * @return
	 */
	public String[] getBind(Material mat) {
		return (String[]) this.binds.get(mat);
	}
	
	/**
	 * Get the total map of binds.
	 * @return
	 */
	public Map<Material, String[]> getBinds() {
		return Collections.unmodifiableMap(this.binds);
	}
	
	/**
	 * Get the remaining cooldown time.
	 * @param name
	 * @return
	 */
	public Long getCooldown(String name) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the total cooldowns list.
	 * @return
	 */
	public Map<String, Long> getCooldowns() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the experience this Alf has for an Alf Class.
	 * @param alfClass
	 * @return
	 */
	public double getExperience(AlfClass alfClass) {
		if (alfClass == null) return 0.0D;
		Double exp = this.experience.get(alfClass.getName());
		return exp == null ? 0.0D : exp;
	}
	
	/**
	 * Get a map of experience types for this Alf.
	 * @return
	 */
	public Map<String, Double> getExperienceMap() {
		return Collections.unmodifiableMap(this.experience);
	}
	
	/**
	 * Get the primary alf class.
	 * @return
	 */
	public AlfClass getAlfClass() {
		this.rwl.readLock().lock();
		AlfClass ac = this.alfClass;
		this.rwl.readLock().unlock();
		return ac;
	}
	
	/**
	 * Get the secondary alf class.
	 * @return
	 */
	public AlfClass getSecondClass() {
		this.rwl.readLock().lock();
		AlfClass sc = this.secondClass;
		this.rwl.readLock().unlock();
		return sc;
	}
	
	/**
	 * Get the max health of this Alf.
	 */
	public int getMaxHealth() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the max mana of this Alf.
	 * @return
	 */
	public int getMaxMana() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the mana for this Alf.
	 * @return
	 */
	public int getMana() {
		return this.mana.get();
	}
	
	/**
	 * Get the amount of mana to be regenerated.
	 * @return
	 */
	public int getManaRegen() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get this Alf's party.
	 * @return
	 */
	public AlfParty getParty() {
		return this.party;
	}

	/**
	 * Get the encapsulated player.
	 * @return
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * Get a map of skills.
	 */
	public Map<String, ConfigurationSection> getSkills() {
		return new HashMap<String, ConfigurationSection>(this.skills);
	}
	
	/**
	 * Get the skill settings.
	 */
	public Map<String, ConfigurationSection> getSkillSettings() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get last cause of damage to this Alf.
	 * @return
	 */
	public AlfDamageCause getLastDamageCause() {
		return this.lastDamageCause;
	}
	
	/**
	 * Get highest level for this player.
	 * @return
	 */
	public int getLevel() {
		this.rwl.readLock().lock();
		int primary = getLevel(this.alfClass);
		int second = 0; 
		if (this.secondClass != null)
			second = getLevel(this.secondClass);
		this.rwl.readLock().unlock();
		return primary > second ? primary : second;
	}
	
	/**
	 * Get the skill level for this Alf's particular skill.
	 * @param skill
	 * @return
	 */
	public int getSkillLevel(Skill skill) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the Alf's level for an AlfClass.
	 * @param alfClass
	 * @return
	 */
	public int getLevel(AlfClass alfClass) {
		return Properties.getLevel(getExperience(alfClass));
	}
	
	/**
	 * Get the Alf's summons.
	 * @return
	 */
	public Set<Monster> getSummons() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Get the Alf's suppressed skills.
	 * @return
	 */
	public Set<String> getSuppressedSkills() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Whether this Alf has a bind matched to the material.
	 * @param mat
	 * @return
	 */
	public boolean hasBind(Material mat) {
		return this.binds.containsKey(mat);
	}
	
	/**
	 * The hash code for this Alf.
	 */
	public int hashCode() {
		return this.player == null ? 0 : this.name.hashCode();
	}
	
	/**
	 * The party of the Alf.
	 * @return
	 */
	public boolean hasParty() {
		return this.party != null;
	}
	
	public boolean canUseSkill(String name) {
		return canUseSkill(this.plugin.getSkillManager().getSkill(name));
	}
	
	/**
	 * Whether the Alf can use the given skill.
	 * @param skill
	 * @return
	 */
	public boolean canUseSkill(Skill skill) {
		if (canPrimaryUseSkill(skill))
			return true;
		if (canSecondUseSkill(skill))
			return true;
		AlfClass secondClass = getSecondClass();
		ConfigurationSection section = (ConfigurationSection)this.skills.get(skill.getName().toLowerCase());
		if (section != null) {
			int level = section.getInt(Setting.LEVEL.node(), 1);
			if (getLevel(getAlfClass()) >= level || (secondClass != null && getLevel(secondClass) >= level))
				return true;
		}
		return false;
	}
	
	/**
	 * Whether the Alf's primary class can use this skill.
	 * @param skill
	 * @return
	 */
	public boolean canPrimaryUseSkill(Skill skill) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Whether the Alf's secondary class can use this skill.
	 * @param skill
	 * @return
	 */
	public boolean canSecondUseSkill(Skill skill) {
		throw new Error("Implement me!");
	}
	
	public boolean hasAccessToSkill(Skill skill) {
		return hasAccessToSkill(skill.getName());
	}
	
	/**
	 * Whether the Alf has access to the named skill.
	 * @param name
	 * @return
	 */
	public boolean hasAccessToSkill(String name) {
		AlfClass secondClass = getSecondClass();
		return (getAlfClass().hasSkill(name) || (secondClass != null) && secondClass.hasSkill(name) 
				|| this.skills.containsKey(name.toLowerCase()));
	}
	
	/**
	 * Whether an Alf has mastered a specific class.
	 * @param alfClass
	 * @return
	 */
	public boolean isMaster(AlfClass alfClass) {
		return getLevel(alfClass) >= alfClass.getMaxLevel();
	}
	
	/**
	 * Whether the Alf has a suppressed skill of the given type.
	 * @param skill
	 * @return
	 */
	public boolean isSuppressing(Skill skill) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Whether to hide/show output messages to the player.
	 * @return
	 */
	public boolean isVerbose() {
		return this.verbose.get();
	}
	
	/**
	 * Get the delayed skill pending for this Alf.
	 * @return
	 */
	public DelayedSkill getDelayedSkill() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Set the new delayed skill pending.
	 * @param wSkill
	 */
	public void setDelayedSkill(DelayedSkill wSkill) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Cancel the pending delayed skill.
	 */
	public void cancelDelayedSkill() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Remove the given cooldown.
	 * @param name
	 */
	public void removeCooldown(String name) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Remove the provided permission.
	 * @param permission
	 */
	public void removePermission(String permission) {
		this.transientPerms.unsetPermission(permission);
		this.player.recalculatePermissions();
	}
	
	/**
	 * Remove the provided permission. 
	 * @param permission
	 */
	public void removePermission(Permission permission) {
		this.transientPerms.unsetPermission(permission);
		this.player.recalculatePermissions();
	}
	
	/**
	 * Remove a skill from the Alf.
	 * @param skill
	 */
	public void removeSkill(String skill) {
		this.skills.remove(skill.toLowerCase());
	}
	
	/**
	 * Set the cooldown for this Alf's specific skill.
	 * @param name
	 * @param cooldown
	 */
	public void setCooldown(String name, long cooldown) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Set the experience for an AlfClass.
	 * @param alfClass
	 * @param experience
	 */
	public void setExperience(AlfClass alfClass, double experience) {
		this.experience.put(alfClass.getName(), experience);
	}
	
	/**
	 * Set the current Alf Class.
	 * @param alfClass
	 * @param secondary
	 */
	public void setAlfClass(AlfClass alfClass, boolean secondary) {
		double currentMaxHP = getMaxHealth();
		this.rwl.writeLock().lock();
		if (secondary)
			this.secondClass = alfClass;
		else 
			this.alfClass = alfClass;
		this.rwl.writeLock().unlock();
		double newMaxHP = getMaxHealth();
		double health = getHealth();
		health *= newMaxHP / currentMaxHP;
		if (health > newMaxHP)
			health = newMaxHP;
		setHealth((int)health);
		
		checkInventory();
	}
	
	/**
	 * Set the last damage cause.
	 * @param lastDamageCause
	 */
	public void setLastDamageCause(AlfDamageCause lastDamageCause) {
		this.lastDamageCause = lastDamageCause;
	}
	
	/**
	 * Set the mana value for this Alf.
	 * @param mana
	 */
	public void setMana(int mana) {
		int maxMana = getMaxMana();
		if (mana > maxMana) 
			mana = maxMana;
		else if (mana < 0)
			mana = 0;
		this.mana.getAndSet(mana);
	}
	
	/**
	 * Set the party for this Alf.
	 * @param party
	 */
	public void setParty(AlfParty party) {
		this.party = party;
	}
	
	/**
	 * Get the skill setting for the given skill's node.
	 * @param skill
	 * @param node
	 * @param val
	 */
	public void setSkillSetting(Skill skill, String node, Object val) {
		setSkillSetting(skill.getName(), node, val);
	}
	
	public void setSkillSetting(String skillName, String node, Object val) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Set whether a skill should be suppressed.
	 * @param skill
	 * @param suppressed
	 */
	public void setSuppressed(Skill skill, boolean suppressed) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Set the suppressed skills for this Alf.
	 * @param suppressedSkills
	 */
	public void setSuppresedSkills(Collection<String> suppressedSkills) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Set whether this Alf is verbose.
	 * @param verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose.getAndSet(verbose);
	}
	
	/**
	 * Get the class for this Alf which has enchanting experience types.
	 * @return
	 */
	public AlfClass getEnchantingClass() {
		throw new Error("Implement me!");
	}
	
	public void syncExperience() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Sync player experience.
	 * @param ac
	 */
	public void syncExperience(AlfClass ac) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Sync player health.
	 */
	public void syncHealth() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Remove a bind from a material.
	 * @param material
	 */
	public void unbind(Material material) {
		this.binds.remove(material);
	}
	
	/**
	 * Check player inventory for disallowed items, etc.
	 */
	public void checkInventory() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Check armor slots for allowed armor.
	 * @return
	 */
	public int checkArmorSlots() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Check whether the item in the given slot id can be equipped.
	 * @param slot
	 * @return
	 */
	public boolean canEquipItem(int slot) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Check whether a certain item can be crafted.
	 * @param o
	 * @return
	 */
	public boolean canCraft(Object o) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Whether to sync primary class stuff.
	 * @return
	 */
	public boolean isSyncPrimary() {
		throw new Error("Implement me!");
	}
	
	/**
	 * Set whether to sync primary class stuff.
	 * @param syncPrimary
	 */
	public void setSyncPrimary(boolean syncPrimary) {
		throw new Error("Implement me!");
	}
	
	/**
	 * Set the player for this Alf.
	 * @param player
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	/**
	 * Reset the combat effect.
	 */
	public void resetCombatEffect() {
		addEffect(this.combat);
	}
	
	/**
	 * Whether the Alf is in combat.
	 * @return
	 */
	public boolean isInCombat() {
		return this.combat.isInCombat();
	}
	
	/**
	 * Whether the Alf is in combat with the target.
	 * @param target
	 * @return
	 */
	public boolean isInCombatWith(LivingEntity target) {
		return this.combat.isInCombatWith(target);
	}
	
	/**
	 * Enter combat with a living entity for a given reason.
	 * @param entity
	 * @param reason
	 * @return
	 */
	public boolean enterCombatWith(LivingEntity entity, CombatEffect.CombatReason reason) {
		boolean start = !this.combat.isInCombat();
		this.combat.enterCombatWith(entity, reason);
		return start;
	}
	
	/**
	 * Refresh the combat effect.
	 */
	public void refreshCombat() {
		this.combat.reset();
	}
	
	/**
	 * Get the combat effect.
	 * @return
	 */
	public CombatEffect getCombatEffect() {
		return this.combat;
	}
	
	/**
	 * Leave combat with 
	 * @param entity
	 * @param reason
	 * @return
	 */
	public boolean leaveCombatWith(LivingEntity entity, CombatEffect.LeaveCombatReason reason) {
		this.combat.leaveCombatWith(this, entity, reason);
		return ! this.combat.isInCombat();
	}
	
	/**
	 * Have the Alf leave combat for a specific reason.
	 * @param reason
	 */
	public void leaveCombat(CombatEffect.LeaveCombatReason reason) {
		switch (reason) {
			case LOGOUT:
				this.combat.leaveCombatFromLogout(this);
				break;
			case DEATH:
				this.combat.leaveCombatFromDeath(this);
				break;
			case SUICIDE:
				this.combat.leaveCombatFromSuicide(this);
			default:
				break;
		}
	}
	
	/**
	 * Get the total combatants this Alf is fighting with.
	 * @return
	 */
	public Map<LivingEntity, CombatEffect.CombatReason> getCombatants() {
		return this.combat.getCombatants();
	}
	
	/**
	 * Get the location being viewed by a player.
	 * @param distance
	 * @return
	 */
	public Location getViewingLocation(double distance) {
		Location location = getPlayer().getLocation();
		//Standard procedure. Get the unit vector and multiply by the block distance magnitude.
		location.add(location.getDirection().normalize().multiply(distance));
		return location;
	}
	
}
