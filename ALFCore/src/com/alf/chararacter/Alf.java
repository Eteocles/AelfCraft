package com.alf.chararacter;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;

import com.alf.AlfCore;
import com.alf.api.AlfDamageCause;
import com.alf.api.event.AlfChangeLevelEvent;
import com.alf.api.event.ExperienceChangeEvent;
import com.alf.chararacter.classes.AlfClass;
import com.alf.chararacter.effect.CombatEffect;
import com.alf.chararacter.party.AlfParty;
import com.alf.skill.DelayedSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.util.Messaging;
import com.alf.util.Properties;
import com.alf.util.Setting;
import com.alf.util.Util;

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
	//Karma
	private AtomicInteger karma = new AtomicInteger(100);
	//Player Party
	private AlfParty party = null;
	//Output messages or not
	private AtomicBoolean verbose = new AtomicBoolean(true);
	//Last damage cause for this Alf.
	private AlfDamageCause lastDamageCause = null;
	//Types of experience.
	private Map<String, Double> experience = new ConcurrentHashMap<String, Double>();
	//Cooldowns
	private Map<String, Long> cooldowns = new ConcurrentHashMap<String, Long>();
	//Set of summons.
	private Set<Monster> summons = new HashSet<Monster>();
	//Binds
	private Map<Material, String[]> binds = new ConcurrentHashMap<Material, String[]>();
	//Suppressed Skills (Can't be used)
	private Map<String, Boolean> suppressedSkills = new ConcurrentHashMap<String, Boolean>();
	//Persistent skill settings.
	private Map<String, ConfigurationSection> persistedSkillSettings = new ConcurrentHashMap<String, 
			ConfigurationSection>();
	//Skills
	private Map<String, ConfigurationSection> skills = new HashMap<String, ConfigurationSection>();
	//Sync primary class info.
	private boolean syncPrimary = true;
	//Tiered level count.
	private Integer tieredLevel;
	//Permission values.
	private PermissionAttachment transientPerms;
	//Alf's delayed skill.
	private DelayedSkill delayedSkill = null;
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
		addEffect(this.combat);
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
		boolean val;
		try {
			this.rwl.readLock().lock();
			val = (this.alfClass.hasExperienceType(type)) || 
					(this.secondClass != null && this.secondClass.hasExperienceType(type));
		} finally {
			this.rwl.readLock().unlock();
		}
		return val;
	}

	/**
	 * Whether this Alf can gain an experience type.
	 * Non-specific to primary or secondary class.
	 * @param type
	 * @return
	 */
	public boolean canGain(AlfClass.ExperienceType type) {
		if (type == AlfClass.ExperienceType.ADMIN)
			return true;
		boolean prim = false;
		this.rwl.readLock().lock();
		//Determine whether the primary class can gain the experience.
		if (this.alfClass.hasExperienceType(type))
			prim = (! isMaster(this.alfClass) || (Properties.padMaxLevel && 
					getExperience(this.alfClass) < Properties.maxExp - 1));
		boolean prof = false;
		//Determine whether the secondary class can gain the experience.
		if (this.secondClass != null && this.secondClass.hasExperienceType(type))
			prof = ! isMaster(this.secondClass) || (Properties.padMaxLevel && 
					(getExperience(this.secondClass) < Properties.maxExp - 1));
		this.rwl.readLock().unlock();
		return prim || prof;
	}

	/**
	 * Bind a skill to a material.
	 * @param material
	 * @param skillName
	 */
	public void bind(Material material, String[] skillName) {
		if (material != Material.AIR && material != null) {
			this.binds.put(material, skillName);
		}
	}

	/**
	 * Change this Alf's class.
	 * @param alfClass
	 * @param secondary - whether the class is a secondary type.
	 */
	public void changeAlfClass(AlfClass alfClass, boolean secondary) {
		//Clear all statuses.
		clearEffects(); //Can this be abused?
		clearSummons();
		clearBinds();
		setAlfClass(alfClass, secondary);
		//If prefixes are enabled...
		if (AlfCore.properties.prefixClassName)
			this.player.setDisplayName("[" + getAlfClass().getName() + "]" + 
					this.player.getName());
		this.plugin.getCharacterManager().performSkillChecks(this);
		getTieredLevel(true);
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
		this.cooldowns.clear();
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
		for (Monster summon : this.summons)
			summon.getEntity().remove();
		this.summons.clear();
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
	 * Only called through GM use.
	 * @param expChange
	 * @param ac
	 * @param loc
	 */
	public void addExp(double expChange, AlfClass ac, Location loc) {
		double exp = getExperience(ac) + expChange;
		if (exp < 0.0D)
			exp = 0.0D;
		int currentLevel = getLevel(ac);
		setExperience(ac, exp);

		//Call an event.
		ExperienceChangeEvent expEvent = new ExperienceChangeEvent(this, ac, expChange, 
				AlfClass.ExperienceType.ADMIN, loc);
		this.plugin.getServer().getPluginManager().callEvent(expEvent);

		//Sync the experience.
		syncExperience();
		int newLevel = Properties.getLevel(exp);
		//If the player changed level...
		if (currentLevel != newLevel) {
			//Call a change level event.
			AlfChangeLevelEvent aLEvent = new AlfChangeLevelEvent(this, ac, currentLevel, newLevel);
			this.plugin.getServer().getPluginManager().callEvent(aLEvent);

			//If the new level surpasses the maximum level. 
			if (newLevel >= ac.getMaxLevel()) {
				setExperience(ac, Properties.getTotalExp(ac.getMaxLevel()));
				Messaging.broadcast(this.plugin, "$1 has become a master $2!", new Object[] {
						this.player.getName(), ac.getName()
				});
			} if (newLevel > currentLevel) {
				Messaging.send(this.player, "You gained a level! (Lvl $1 $2)", new Object[] {
						newLevel, ac.getName()});
				setHealth(getMaxHealth());
				if (this.player.getFoodLevel() < 20)
					this.player.setFoodLevel(20);
				this.player.setExhaustion(0);
				syncHealth();
				getTieredLevel(true);
			} else {
				if (getHealth() > getMaxHealth()) {
					setHealth(getMaxHealth());
					syncHealth();
				}
				Messaging.send(this.player, "You just lost a level! (Lvl $1 $2)",
						new Object[] { newLevel, ac.getName() });
			}
		}
	}

	/**
	 * The Alf gains Exp through a specific means.
	 * @param expChange
	 * @param source
	 * @param loc
	 */
	public void gainExp(double expChange, AlfClass.ExperienceType source, Location loc) {
		if (this.player.getGameMode() == GameMode.SURVIVAL) {
			Properties prop = AlfCore.properties;
			AlfClass[] classes = { getAlfClass(), getSecondClass() };
			//Iterate through both primary and secondary classes and add exp where necessary.
			for (AlfClass ac : classes) {
				if (ac != null) {
					if (source == AlfClass.ExperienceType.ADMIN || ac.hasExperienceType(source)) {
						double gainedExp = expChange;
						double exp = getExperience(ac);

						//Grant experience with mod only if it's positive and granted through ordinary means.
						if (gainedExp > 0.0D && source != AlfClass.ExperienceType.ADMIN)
							gainedExp *= ac.getExpModifier();
						//If experience gain is of ordinary means and the player has Mastered this class.
						else if (source != AlfClass.ExperienceType.ADMIN && source != AlfClass.ExperienceType.ENCHANTING &&
								isMaster(ac) && ! prop.levelsViaExpLoss) {
							return;
						}
						//Call the experience change event.
						ExperienceChangeEvent expEvent = new ExperienceChangeEvent(this, ac, gainedExp, source, loc);
						this.plugin.getServer().getPluginManager().callEvent(expEvent);

						//If the event isn't cancelled, continue.
						if (expEvent.isCancelled())
							return;

						gainedExp = expEvent.getExpChange();
						int currentLevel = Properties.getLevel(exp);
						int newLevel = Properties.getLevel(exp + gainedExp);

						if (isMaster(ac) && currentLevel > newLevel && ! prop.levelsViaExpLoss && 
								source != AlfClass.ExperienceType.ADMIN && source != AlfClass.ExperienceType.ENCHANTING) {
							gainedExp = Properties.getTotalExp(currentLevel) - (exp - 1.0D);
						} else if (isMaster(ac) && newLevel > currentLevel) {
							gainedExp = 0.0D;
							continue;
						}

						exp += gainedExp;
						if (exp < 0.0D) {
							gainedExp = -exp;
							exp = 0.0D;
						} else if (exp > Properties.maxExp) {
							exp = Properties.maxExp;
						}

						//Get the new level.
						newLevel = Properties.getLevel(exp);
						setExperience(ac, exp);

						if (gainedExp != 0.0D) {
							if (isVerbose() && gainedExp > 0.0D)
								Messaging.send(this.player, "$1: Gained $2 Exp", new Object[] { ac.getName(),
										decFormat.format(gainedExp) });
							else if (isVerbose() && gainedExp < 0.0D)
								Messaging.send(this.player, "$1: Lost $2 Exp", new Object[] { ac.getName(),
										decFormat.format(- gainedExp) });
							if (newLevel != currentLevel && newLevel <= ac.getMaxLevel()) {
								AlfChangeLevelEvent aLEvent = new AlfChangeLevelEvent(this, ac, currentLevel, newLevel);
								this.plugin.getServer().getPluginManager().callEvent(aLEvent);

								if (newLevel == ac.getMaxLevel())
									Messaging.broadcast(this.plugin, "$1 has become a master $2!", new Object[] {
											this.player.getName(), ac.getName() });

								if (newLevel > currentLevel) {
									Messaging.send(this.player, "You gained a level! (Lvl $1 $2)", new Object[] {
											newLevel, ac.getName() });
									if (getHealth() > 0) {
										setHealth(getMaxHealth());
										setMana(getMaxMana());
										if (this.player.getFoodLevel() < 20)
											this.player.setFoodLevel(20);
										this.player.setSaturation(1.0F);
										this.player.setExhaustion(0.0F);
										syncHealth();
									}
									getTieredLevel(true);
								} else Messaging.send(this.player, "You just lost a level! (Lvl $1 $2)", new Object[] {
										newLevel, ac.getName() });
							}
						}
						if (newLevel != currentLevel)
							this.plugin.getCharacterManager().saveAlf(this, false);

					}
				}
			}
		}
		syncExperience();
	}

	/**
	 * Get the amount of exp needed to get to the next level for the alf class.
	 * @param ac
	 * @return
	 */
	public double currentEXPToNextLevel(AlfClass ac) {
		return getExperience(ac) - Properties.getTotalExp(getLevel(ac));
	}

	/**
	 * Get the amount of exp that should be lost, given the proximity to a level and the amount of experience.
	 * @param multiplier
	 * @param ac
	 * @return
	 */
	protected double calculateEXPLoss(double multiplier, AlfClass ac) {
		//Get the amount of exp needed for the next level.
		double expForNext = Properties.getExp(getLevel(ac) + 1);
		//Get the percentage of current exp of the total exp needed.
		double currentPercent = currentEXPToNextLevel(ac) / expForNext;

		if (currentPercent >= multiplier)
			return expForNext * multiplier;
		//Amount of experience required.
		double amt = expForNext * currentPercent;
		multiplier -= currentPercent;

		//Iterate through all of a player's levels 
		for (int i = 0; getLevel(ac) - i > 1; i++) {
			if (1.0D >= multiplier)
				return amt += Properties.getExp(getLevel(ac) - i) * multiplier;
			amt += Properties.getExp(getLevel(ac) - i);
			multiplier -= 1.0D;
		}
		return amt;
	}

	/**
	 * Have a player lose experience from death.
	 * @param multiplier - multiplier for loss
	 * @param pvp - whether the death was from pvp
	 */
	public void loseExpFromDeath(double multiplier, boolean pvp) {
		if (this.player.getGameMode() == GameMode.SURVIVAL && multiplier > 0.0D) {
			Properties prop = AlfCore.properties;
			AlfClass classes[] = { getAlfClass(), getSecondClass() };
			if (prop.resetOnDeath) { //TODO Allow players in Hardcore Duels...
				clearExperience();
				setAlfClass(null, true);
				setAlfClass(this.plugin.getClassManager().getDefaultClass(), false);
				Messaging.send(this.player, "You've lost all your experience and have been reset to $1 for dying!",
						new Object[] { this.plugin.getClassManager().getDefaultClass().getName() });
				this.plugin.getCharacterManager().saveAlf(this, false);
				syncExperience();
				return;
			}
			for (AlfClass ac : classes) {
				if (ac != null) {
					double mult = multiplier;
					if (pvp && ac.getPvpExpLoss() != -1.0D)
						mult = ac.getPvpExpLoss();
					else if (! pvp && ac.getExpLoss() != -1.0D)
						mult = ac.getExpLoss();

					int currentLvl = getLevel(ac);
					double currentExp = getExperience(ac);
					double currentLvlExp = Properties.getTotalExp(currentLvl);
					double gainedExp = -calculateEXPLoss(mult, ac);

					//If levels can't be lost via exp loss, set the current exp to the level's minimum.
					if (gainedExp + currentExp < currentLvlExp && ! prop.levelsViaExpLoss)
						gainedExp = -(currentExp - currentLvlExp);

					//Call the event.
					ExperienceChangeEvent expEvent = new ExperienceChangeEvent(this, ac, gainedExp,
							AlfClass.ExperienceType.DEATH, this.player.getLocation());
					this.plugin.getServer().getPluginManager().callEvent(expEvent);
					if (expEvent.isCancelled())
						return;
					//Update gainedExp in case the event was modified.
					gainedExp = expEvent.getExpChange();

					int newLevel = Properties.getLevel(currentExp + gainedExp);
					if (! isMaster(ac) || prop.masteryLoss) {
						if (currentLvl > newLevel && ! prop.levelsViaExpLoss)
							gainedExp = currentLvlExp - (currentExp - 1.0D);
						double newExp = currentExp + gainedExp;

						//Don't go under zero.
						if (newExp < 0.0D) {
							gainedExp = -currentExp;
							newExp = 0.0D;
						}

						//Get the new level and update experience.
						newLevel = Properties.getLevel(newExp);
						setExperience(ac, newExp);

						//If experience has changed.
						if (gainedExp != 0.0D) {
							if (isVerbose() && gainedExp < 0.0D)
								Messaging.send(this.player, "$1: Lost $2 Exp", new Object[] { ac.getName(), 
										decFormat.format(- gainedExp) });
							if (newLevel != currentLvl) {
								AlfChangeLevelEvent aLEvent = new AlfChangeLevelEvent(this, ac, currentLvl, newLevel);
								this.plugin.getServer().getPluginManager().callEvent(aLEvent);
								if (newLevel >= ac.getMaxLevel()) {
									setExperience(ac, Properties.getTotalExp(ac.getMaxLevel()));
									Messaging.broadcast(this.plugin, "$1 has become a master $2!", new Object[] {
											this.player.getName(), ac.getName() });
								}
								Messaging.send(this.player, "You lost a level! (Lvl $1 $2)", new Object[] {
										newLevel, ac.getName() });
							}
						}
					}
				}
			}
			this.plugin.getCharacterManager().saveAlf(this, false);
			syncExperience();
		}
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
		return this.cooldowns.get(name.toLowerCase());
	}

	/**
	 * Get the total cooldowns list.
	 * @return
	 */
	public Map<String, Long> getCooldowns() {
		return Collections.unmodifiableMap(this.cooldowns);
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
		AlfClass alfClass = getAlfClass();
		int level = Properties.getLevel(getExperience(alfClass));
		int primaryHP = alfClass.getBaseMaxHealth() + (int)((level - 1) * alfClass.getMaxHealthPerLevel());
		int secondHP = 0;
		AlfClass secondClass = getSecondClass();
		if (secondClass != null) {
			level = Properties.getLevel(getExperience(secondClass));
			secondHP = secondClass.getBaseMaxHealth() + (int)((level - 1) * secondClass.getMaxHealthPerLevel());
		}
		return primaryHP > secondHP ? primaryHP : secondHP;
	}

	/**
	 * Get the max mana of this Alf.
	 * @return
	 */
	public int getMaxMana() {
		AlfClass alfClass = getAlfClass();
		int level = Properties.getLevel(getExperience(alfClass));
		int primaryMana = alfClass.getBaseMaxMana() + (int)((level - 1) * alfClass.getMaxManaPerLevel());
		int secondMana = 0;
		AlfClass secondClass = getSecondClass();
		if (secondClass != null) {
			level = Properties.getLevel(getExperience(secondClass));
			secondMana = secondClass.getBaseMaxMana() + (int)((level - 1) * secondClass.getMaxManaPerLevel());
		}
		return primaryMana > secondMana ? primaryMana : secondMana;
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
		AlfClass alfClass = getAlfClass();
		int level = Properties.getLevel(getExperience(alfClass));
		double primaryMana = alfClass.getManaRegen() + (level - 1) * alfClass.getManaRegenPerLevel();
		double secondMana = 0.0D;
		AlfClass secondClass = getSecondClass();
		if (secondClass != null) {
			level = Properties.getLevel(getExperience(secondClass));
			secondMana = secondClass.getManaRegen() + (level - 1) * secondClass.getManaRegenPerLevel();
		}
		return (int)(primaryMana > secondMana ? primaryMana : secondMana);
	}
	
	/**
	 * Get this player's karma.
	 * @return
	 */
	public int getKarma() {
		return this.karma.get();
	}
	
	/**
	 * Set this player's karma.
	 * @param newKarma
	 */
	public void setKarma(int newKarma) {
		this.karma.getAndSet(newKarma);
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
		return Collections.unmodifiableMap(this.persistedSkillSettings);
	}

	/**
	 * Get the skill setting for a specific skill type.
	 * @param skillName
	 * @return
	 */
	public ConfigurationSection getSkillSettings(String skillName) {
		AlfClass secondClass = getSecondClass();
		if (! getAlfClass().hasSkill(skillName) && (secondClass == null || ! secondClass.hasSkill(skillName)))
			return null;
		return (ConfigurationSection) this.persistedSkillSettings.get(skillName.toLowerCase());
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
		int level = -1, secondLevel = -1;
		AlfClass alfClass = getAlfClass();
		if (alfClass.hasSkill(skill.getName())) {
			int requiredLevel = SkillConfigManager.getSetting(alfClass, skill, Setting.LEVEL.node(), 1);
			level = getLevel(alfClass);
			if (level < requiredLevel)
				level = -1;
		}
		AlfClass secondClass = getSecondClass();
		if (secondClass != null && secondClass.hasSkill(skill.getName())) {
			int requiredLevel = SkillConfigManager.getSetting(secondClass, skill, Setting.LEVEL.node(), 1);
			secondLevel = getLevel(secondClass);
			if (secondLevel < requiredLevel)
				secondLevel = -1;
		}
		return secondLevel > level ? secondLevel : level;
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
	 * Get the tiered level of this Alf.
	 * @param recache
	 * @return
	 */
	public int getTieredLevel(boolean recache) {
		if (this.tieredLevel != null && ! recache)
			return this.tieredLevel;
		AlfClass alfClass = getAlfClass();
		AlfClass secondClass = getSecondClass();

		if (secondClass == null)
			this.tieredLevel = getTieredLevel(alfClass);
		else {
			int ac = getTieredLevel(alfClass);
			int sc = getTieredLevel(secondClass);
			this.tieredLevel = ac > sc ? ac : sc;
		}
		return this.tieredLevel;
	}

	/**
	 * Get the tiered level (Sum of Levels) for an Alf Class and its tiers.
	 * @param alfClass
	 * @return
	 */
	public int getTieredLevel(AlfClass alfClass) {
		if (alfClass.hasNoParents())
			return getLevel(alfClass);

		Set<AlfClass> classes = new HashSet<AlfClass>();
		for (AlfClass aClass : alfClass.getParents()) {
			if (isMaster(aClass)) {
				classes.addAll(getTieredLevel(aClass, new HashSet<AlfClass>(classes)));
				classes.add(aClass);
			}
		}
		int level = getLevel(alfClass);
		for (AlfClass aClass : classes)
			if (aClass.getTier() != 0)
				level += getLevel(aClass);
		return level;
	}

	/**
	 * Get a set of all the tiers of an AlfClass.
	 * @param alfClass
	 * @param classes
	 * @return
	 */
	private Set<AlfClass> getTieredLevel(AlfClass alfClass, Set<AlfClass> classes) {
		for (AlfClass aClass : alfClass.getParents()) {
			if (isMaster(aClass)) {
				classes.addAll(getTieredLevel(aClass, new HashSet<AlfClass>(classes)));
				classes.add(aClass);
			}
		}
		return classes;
	}

	/**
	 * Get the Alf's summons.
	 * @return
	 */
	public Set<Monster> getSummons() {
		return this.summons;
	}

	/**
	 * Get the Alf's suppressed skills.
	 * @return
	 */
	public Set<String> getSuppressedSkills() {
		return this.suppressedSkills.keySet();
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
		AlfClass alfClass = getAlfClass();
		if (alfClass.hasSkill(skill.getName())) {
			int level = SkillConfigManager.getSetting(alfClass, skill, Setting.LEVEL.node(), 1);
			if (getLevel(alfClass) >= level)
				return true;
		}
		return false;
	}

	/**
	 * Whether the Alf's secondary class can use this skill.
	 * @param skill
	 * @return
	 */
	public boolean canSecondUseSkill(Skill skill) {
		AlfClass secondClass = getSecondClass();
		if (secondClass != null && secondClass.hasSkill(skill.getName())) {
			int level = SkillConfigManager.getSetting(secondClass, skill, Setting.LEVEL.node(), 1);
			if (getLevel(secondClass) >= level)
				return true;
		}
		return false;
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
		return this.suppressedSkills.containsKey(skill.getName());
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
		return this.delayedSkill;
	}

	/**
	 * Set the new delayed skill pending.
	 * @param wSkill
	 */
	public void setDelayedSkill(DelayedSkill wSkill) {
		this.delayedSkill = wSkill;
	}

	/**
	 * Cancel the pending delayed skill.
	 */
	public void cancelDelayedSkill() {
		if (this.delayedSkill != null) {
			Skill skill = this.delayedSkill.getSkill();
			this.delayedSkill = null;
			skill.broadcast(this.player.getLocation(), "$1 has stopped using $2!", new Object[] {
				this.player.getDisplayName(), skill.getName()
			});
		}
	}

	/**
	 * Remove the given cooldown.
	 * @param name
	 */
	public void removeCooldown(String name) {
		this.cooldowns.remove(name.toLowerCase());
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
		this.cooldowns.put(name.toLowerCase(), cooldown);
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
		ConfigurationSection section = this.persistedSkillSettings.get(skillName.toLowerCase());
		if (section == null) {
			section = new MemoryConfiguration();
			this.persistedSkillSettings.put(skillName.toLowerCase(), section);
		}
		section.set(node, val);
	}

	/**
	 * Set whether a skill should be suppressed.
	 * @param skill
	 * @param suppressed
	 */
	public void setSuppressed(Skill skill, boolean suppressed) {
		if (suppressed)
			this.suppressedSkills.put(skill.getName(), true);
		else
			this.suppressedSkills.remove(skill.getName());
	}

	/**
	 * Set the suppressed skills for this Alf.
	 * @param suppressedSkills
	 */
	public void setSuppressedSkills(Collection<String> suppressedSkills) {
		for (String s : suppressedSkills)
			this.suppressedSkills.put(s, true);
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
		AlfClass alfClass = getAlfClass();
		AlfClass secondClass = getSecondClass();
		int level = 0;
		if (alfClass.hasExperienceType(AlfClass.ExperienceType.ENCHANTING))
			level = getLevel(alfClass);
		if (secondClass != null && secondClass.hasExperienceType(AlfClass.ExperienceType.ENCHANTING) && 
				getLevel(secondClass) > level)
			return secondClass;
		return level != 0 ? alfClass : null;
	}

	/**
	 * Sync total experience.
	 */
	public void syncExperience() {
		AlfClass secondClass = getSecondClass();
		if (secondClass != null && ! this.syncPrimary)
			syncExperience(secondClass);
		else syncExperience(getAlfClass());
	}

	/**
	 * Sync experience with the contained player.
	 * @param ac
	 */
	public void syncExperience(AlfClass ac) {
		int level = getLevel(ac);
		//Amount of XP to reach the current level.
		int currentLevelXP = Properties.getTotalExp(level);
		//Amount of XP to reach the next level from the current level minimum.
		double maxLevelXP = Properties.getTotalExp(level + 1) - currentLevelXP;
		double currentXP = getExperience(ac) - currentLevelXP;
		//Get current percentage of XP required to get to the next level.
		float syncedPercent = (float)(currentXP / maxLevelXP);
		//		this.player.setTotalExperience(Util.getMCExperience(level));
		//Set the percentage towards the next level.
		this.player.setExp(syncedPercent);
		//Set the current experience level.
		this.player.setLevel(level);
	}

	/**
	 * Sync health with the contained player.
	 */
	public void syncHealth() {
		double health = getHealth();
		if ((this.player.isDead() || this.player.getHealth() == 0) && health <= 0.0D)
			return;
		int playerHealth = (int) (health / getMaxHealth() * 20.0D);
		//Careful for truncation error.
		if (playerHealth == 0 && health > 0.0D)
			playerHealth = 1;
		this.player.setHealth(playerHealth);
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
		if (this.player.getGameMode() == GameMode.SURVIVAL) {
			int removedCount = checkArmorSlots();
			for (int i = 0; i < 9; i++) 
				if (! canEquipItem(i))
					removedCount++;
			if (removedCount > 0) {
				Messaging.send(this.player, "$1 have been removed from your inventory due to class restrictions.", 
						new Object[] { removedCount + " Items" });
				Util.syncInventory(this.player, this.plugin);
			}
		}
	}

	/**
	 * Check armor slots for allowed armor.
	 * @return
	 */
	public int checkArmorSlots() {
		PlayerInventory inv = this.player.getInventory();
		int removedCount = 0;

		AlfClass alfClass = getAlfClass();
		AlfClass secondClass = getSecondClass();
		int hatsLevel = AlfCore.properties.hatsLevel;
		if (inv.getHelmet() != null && inv.getHelmet().getTypeId() != 0) {
			Material item = inv.getHelmet().getType();
			if ((Util.isArmor(item) || ! AlfCore.properties.allowHats || 
					(AlfCore.properties.allowHats && getLevel(alfClass) < hatsLevel && 
							(secondClass == null || getLevel(secondClass)  < hatsLevel))) &&
							! alfClass.isAllowedArmor(item) && (secondClass == null || ! secondClass.isAllowedArmor(item))) {
				Util.moveItem(this, -1, inv.getHelmet());
				inv.setHelmet(null);
				removedCount++;
			}
		}

		if (inv.getChestplate() != null && inv.getChestplate().getTypeId() != 0) {
			Material item = inv.getChestplate().getType();
			if (! alfClass.isAllowedArmor(item) && (secondClass == null || ! secondClass.isAllowedArmor(item))) {
				Util.moveItem(this, -1, inv.getChestplate());
				inv.setChestplate(null);
				removedCount++;
			}
		}

		if (inv.getLeggings() != null && inv.getLeggings().getTypeId() != 0) {
			Material item = inv.getLeggings().getType();
			if (! alfClass.isAllowedArmor(item) && (secondClass == null || ! secondClass.isAllowedArmor(item))) {
				Util.moveItem(this, -1, inv.getLeggings());
				inv.setLeggings(null);
				removedCount++;
			}
		}

		if (inv.getBoots() != null && inv.getBoots().getTypeId() != 0) {
			Material item = inv.getBoots().getType();
			if (! alfClass.isAllowedArmor(item) && (secondClass == null || ! secondClass.isAllowedArmor(item))) {
				Util.moveItem(this, -1, inv.getBoots());
				inv.setBoots(null);
				removedCount++;
			}
		}

		return removedCount;
	}

	/**
	 * Check whether the item in the given slot id can be equipped.
	 * @param slot
	 * @return
	 */
	public boolean canEquipItem(int slot) {
		ItemStack itemStack = this.player.getInventory().getItem(slot);
		if (itemStack == null)
			return true;
		AlfClass secondClass = getSecondClass();
		Material itemType = itemStack.getType();
		if (! Util.isWeapon(itemType))
			return true;
		//Only check if the item is a weapon.
		if (getAlfClass().isAllowedWeapon(itemType) || (secondClass != null && secondClass.isAllowedWeapon(itemType)))
			return true;
		Util.moveItem(this, slot, itemStack);
		return false;
	}

	/**
	 * Check whether a certain item can be crafted.
	 * @param o
	 * @return
	 */
	public boolean canCraft(Object o) {
		if (o instanceof ItemStack && ((ItemStack)o).getType() == Material.MAP)
			o = Material.MAP;
		AlfClass alfClass = getAlfClass();
		int level = alfClass.getCraftLevel(o);
		if (level != -1 && level <= getLevel(alfClass))
			return true;
		
		AlfClass secondClass = getSecondClass();
		if (secondClass != null) {
			level = secondClass.getCraftLevel(o);
			if (level != -1 && level <= getLevel(secondClass))
				return true;
		}
		return false;
	}

	/**
	 * Whether to sync primary class stuff.
	 * @return
	 */
	public boolean isSyncPrimary() {
		return this.syncPrimary;
	}

	/**
	 * Set whether to sync primary class stuff.
	 * @param syncPrimary
	 */
	public void setSyncPrimary(boolean syncPrimary) {
		this.syncPrimary = (syncPrimary || getSecondClass() == null);
		syncExperience();
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
