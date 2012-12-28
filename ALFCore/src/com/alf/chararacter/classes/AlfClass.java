package com.alf.chararacter.classes;

import java.util.*;

import org.bukkit.Material;

import com.alf.AlfCore;
import com.alf.chararacter.CharacterDamageManager;
import com.alf.util.RecipeGroup;

/**
 * Describes a class specification for an Alf.
 * @author Eteocles
 */
public class AlfClass implements Comparable<AlfClass> {

	private final String name;
	private String description;
	private Set<AlfClass> strongParents = new HashSet<AlfClass>();
	private Set<AlfClass> weakParents = new HashSet<AlfClass>();
	private Set<AlfClass> specializations = new LinkedHashSet<AlfClass>();
	private Set<Material> allowedArmor = EnumSet.noneOf(Material.class);
	private Set<Material> allowedWeapons = EnumSet.noneOf(Material.class);
	private Set<ExperienceType> experienceSources = null;
	private boolean primary = true;
	private boolean secondary = false;
	private int tier = 1;
	private Map<Material, Integer> itemDamage = new EnumMap<Material, Integer>(Material.class);
	private Map<Material, Double> itemDamageLevel = new EnumMap<Material, Double>(Material.class);
	private Map<CharacterDamageManager.ProjectileType, Integer> projectileDamage = new EnumMap<CharacterDamageManager.ProjectileType, Integer>(CharacterDamageManager.ProjectileType.class);
	private Map<CharacterDamageManager.ProjectileType, Double> projDamageLevel = new EnumMap<CharacterDamageManager.ProjectileType, Double>(CharacterDamageManager.ProjectileType.class);
	private Set<String> skills = new LinkedHashSet<String>();
	private List<RecipeGroup> recipes = new ArrayList<RecipeGroup>();
	private double cost;
	private double expModifier;
	private double expLoss;
	private double pvpExpLoss;
	private int maxLevel;
	private int baseMaxHealth;
	private int baseMaxMana;
	private double maxHealthPerLevel;
	private double maxManaPerLevel;
	private double manaRegen;
	private double manaRegenPerLevel;
	private boolean wildcard = true;
	private final AlfCore plugin;
	private int manaColor;
	
	/**
	 * Thrown when a class inherits itself / contains itself in its inheritance tree.
	 * @author Eteocles
	 */
	public class CircularParentException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}
	
	/**
	 * Types of experience that can be gained.
	 * @author Eteocles
	 */
	public static enum ExperienceType
	{
		//Non-Profession Experience Types
		SKILL,
		KILLING,
		PVP,
		DEATH,
		ADMIN,
		EXTERNAL,
		QUESTING,
		//Profession Experience Types
		MINING, 
		ENCHANTING, SMITHING,
		LOGGING,
		FISHING,
		FARMING, SHEARING, BREEDING,
		ENGINEERING,
		BREWING, IMBUING,
		TRADING
	}
	
	/**
	 * Construct the AlfClass.
	 * @param name
	 * @param plugin
	 */
	public AlfClass(String name, AlfCore plugin) {
		this.name = name;
		this.plugin = plugin;
		this.description = "";
		this.expModifier = 1.0D;
		this.baseMaxHealth = 20;
		this.maxHealthPerLevel = 0.0D;
		this.maxLevel = 1;
		this.cost = 0.0D;
	}

	/**
	 * Get the class's name.
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Add the material as an allowed armor item.
	 * @param armor
	 */
	protected void addAllowedArmor(Material armor) {
		this.allowedArmor.add(armor);
	}
	
	/**
	 * Add the material as an allowed weapon item.
	 * @param weapon
	 */
	protected void addAllowedWeapon(Material weapon) {
		this.allowedWeapons.add(weapon);
	}

	/**
	 * Add a skill to this class.
	 * @param name
	 */
	protected void addSkill(String name) {
		this.skills.add(name.toLowerCase());
	}
	
	/**
	 * Add a specialization to this class.
	 * @param alfClass
	 */
	public void addSpecialization(AlfClass alfClass) {
		this.specializations.add(alfClass);
	}
	
	/**
	 * Add a class as a strong parent.
	 */
	public void addStrongParent(AlfClass parent) throws CircularParentException {
		if (parent.equals(this))
			throw new CircularParentException();
		List<AlfClass> parents = parent.getParents();
		for (AlfClass ac : parents) {
			if (ac.equals(this)) 
				throw new CircularParentException();
			ac.checkCircular(this);
		}
	}
	
	/**
	 * 
	 * @param parent
	 * @throws CircularParentException
	 */
	public void addWeakParent(AlfClass parent) throws CircularParentException {
		if (parent.equals(this))
			throw new CircularParentException();
		List<AlfClass> parents = parent.getParents();
		for (AlfClass ac : parents) {
			if (ac.equals(this))
				throw new CircularParentException();
			ac.checkCircular(this);
		}
	}
	
	/**
	 * Check whether the alf class is circular.
	 * @param alfClass
	 * @throws CircularParentException
	 */
	private void checkCircular(AlfClass alfClass) throws CircularParentException {
		List<AlfClass> parents = getParents();
		for (AlfClass ac : parents) {
			if (ac.equals(alfClass))
				throw new CircularParentException();
			ac.checkCircular(alfClass);
		}
	}
	
	/**
	 * Get this class's parents.
	 * @return
	 */
	public List<AlfClass> getParents() {	
		List<AlfClass> parents = new ArrayList<AlfClass>(this.strongParents);
		parents.addAll(this.weakParents);
		return Collections.unmodifiableList(parents);
	}
	
	/** Get the crafting level for a specific object. */
	public int getCraftLevel(Object o) {
		int level = -1; 
		for (RecipeGroup rg : this.recipes) {
			Boolean b = rg.get(o);
			if (b != null) {
				if (!b)
					return -1;
				if (level == -1)
					level = rg.level;
				else if (rg.level < level)
					level = rg.level;
			}
		}
		return level;
	}

	/**
	 * Whether this class is the default class.
	 * @return
	 */
	public boolean isDefault()
	{	return this.plugin.getClassManager().getDefaultClass().equals(this);	}

	/**
	 * Whether this is a primary class.
	 * @return
	 */
	public boolean isPrimary()
	{	return this.primary;	}

	/**
	 * Set whether this is a primary class.
	 * @param primary
	 */
	protected void setPrimary(boolean primary)
	{	this.primary = primary;	}

	/**
	 * Whether this is a secondary class.
	 * @return
	 */
	public boolean isSecondary()
	{	return this.secondary;	}

	/**
	 * Set whether this is a secondary class.
	 * @param secondary
	 */
	public void setSecondary(boolean secondary)
	{	this.secondary = secondary;	}

	/**
	 * Get the tier for this class.
	 * @return
	 */
	public int getTier()
	{	return this.tier;	}

	/**
	 * Set the tier for this class.
	 * @param tier
	 */
	protected void setTier(int tier)
	{	this.tier = tier;	}

	/** Define Equality for the AlfClass */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof AlfClass)) return false;
		AlfClass other = (AlfClass) obj;
		if (this.name.equalsIgnoreCase(other.name))
			return true;
		return false;
	}

	/**
	 * Whether a material is an allowed armor type.
	 * @param mat
	 * @return
	 */
	public boolean isAllowedArmor(Material mat)
	{	return this.allowedArmor.contains(mat);	}

	/**
	 * Get the total set of allowed armor.
	 * @return
	 */
	public Set<Material> getAllowedArmor()
	{	return Collections.unmodifiableSet(this.allowedArmor);	}

	/**
	 * Whether the material is an allowed weapon.
	 * @param mat
	 * @return
	 */
	public boolean isAllowedWeapon(Material mat)
	{	return this.allowedWeapons.contains(mat);	}

	/**
	 * Get the total set of allowed weapons.
	 * @return
	 */
	public Set<Material> getAllowedWeapons()
	{	return Collections.unmodifiableSet(this.allowedWeapons);	}

	/**
	 * Get the base max health for this class.
	 * @return
	 */
	public int getBaseMaxHealth()
	{	return this.baseMaxHealth;	}

	/**
	 * Get the base max mana for this class.
	 * @return
	 */
	public int getBaseMaxMana()
	{	return this.baseMaxMana;	}

	/**
	 * Get the cost for this class.
	 * @return
	 */
	public double getCost()
	{	return this.cost;	}

	/**
	 * Get the class's description.
	 * @return
	 */
	public String getDescription()
	{	return this.description;	}

	/**
	 * Get the experience sources for this class.
	 * @return
	 */
	public Set<ExperienceType> getExperienceSources()
	{	return Collections.unmodifiableSet(this.experienceSources);	}

	/**
	 * Whether this class has a given experience type.
	 * @param type
	 * @return
	 */
	public boolean hasExperienceType(ExperienceType type)
	{	return this.experienceSources.contains(type);	}

	/**
	 * Get experience loss for this class on death.
	 * @return
	 */
	public double getExpLoss()
	{	return this.expLoss;	}

	/**
	 * Get the experience modifier for this class.
	 * @return
	 */
	public double getExpModifier()
	{	return this.expModifier;	}

	/**
	 * Get the amount of damage dealt by a material.
	 * @param material
	 * @return
	 */
	public Integer getItemDamage(Material material)
	{	return (Integer)this.itemDamage.get(material);	}

	/**
	 * Get the damage level for a material.
	 * @param mat
	 * @return
	 */
	public double getItemDamageLevel(Material mat)
	{	return this.itemDamageLevel.containsKey(mat) ? ((Double)this.itemDamageLevel.get(mat)).doubleValue() : 0.0D;	}

	/**
	 * Get the maximum amount of health added per level.
	 * @return
	 */
	public double getMaxHealthPerLevel()
	{	return this.maxHealthPerLevel;	}

	/**
	 * Get the maximum amount of mana per level.
	 * @return
	 */
	public double getMaxManaPerLevel()
	{	return this.maxManaPerLevel;	}

	/**
	 * Get the maximum level for the Alf Class.
	 * @return
	 */
	public int getMaxLevel()
	{	return this.maxLevel;	}

	
	/** Get base projectile damage of type. */
	public Integer getProjectileDamage(CharacterDamageManager.ProjectileType type)
	{	return (Integer)this.projectileDamage.get(type);	}

	/** Get the scaling factor for Projectile Damage with Level. */
	public double getProjDamageLevel(CharacterDamageManager.ProjectileType type) 
	{	return this.projDamageLevel.containsKey(type) ? ((Double)this.projDamageLevel.get(type)).doubleValue() : 0.0D;	}

	/** Get set of all skills. */
	public Set<String> getSkillNames()
	{	return new TreeSet<String>(this.skills);	}

	/** Get set of all specialized forms of this class. */
	public Set<AlfClass> getSpecializations()
	{	return Collections.unmodifiableSet(this.specializations);	}

	/** Get set of all Strong Parents.*/ 
	public Set<AlfClass> getStrongParents()
	{	return Collections.unmodifiableSet(this.strongParents);	}

	/** Get set of all Weak Parents. */
	public Set<AlfClass> getWeakParents()
	{	return Collections.unmodifiableSet(this.weakParents);	}

	public int hashCode()
	{	return this.name == null ? 0 : this.name.hashCode();	}

	/** Check whether or not a particular skill belongs to this class. */
	public boolean hasSkill(String name)
	{	return this.skills.contains(name.toLowerCase());	}

	/** Determine whether this class has a parent. */
	public boolean hasNoParents()
	{	return (this.strongParents.isEmpty()) && (this.weakParents.isEmpty());	}

	/** Alter base max health. */
	protected void setBaseMaxHealth(int baseMaxHealth) 
	{	this.baseMaxHealth = baseMaxHealth;	}

	/** Alter base max mana for this class. */
	protected void setBaseMaxMana(int baseMaxMana) 
	{	this.baseMaxMana = baseMaxMana;	}

	/** Alter cost for switching into this class. */
	protected void setCost(double cost) 
	{	this.cost = cost;	}

	protected void setDescription(String description) 
	{	this.description = description;	}

	/** Alter what experience types generate experience for the AlfClass. */
	protected void setExperienceSources(Set<ExperienceType> experienceSources) {
		this.experienceSources = experienceSources;
	}

	/** Alter exp-loss. */
	protected void setExpLoss(double expLoss) 
	{	this.expLoss = expLoss;	}

	/** Get exp-loss through PVP. */
	public double getPvpExpLoss() 
	{	return this.pvpExpLoss;	}

	/** Alter exp-loss through PVP. */
	public void setPvpExpLoss(double pvpExpLoss) 
	{	this.pvpExpLoss = pvpExpLoss;	}

	/** Alter exp-modifier. */
	protected void setExpModifier(double modifier) 
	{	this.expModifier = modifier;	}

	/** Alter a particular item's base damage. */
	protected void setItemDamage(Material material, int damage) 
	{	this.itemDamage.put(material, Integer.valueOf(damage));	}

	/** Alter a particular item's damage scaling factor. */
	protected void setItemDamageLevel(Material material, double damage) 
	{	this.itemDamageLevel.put(material, Double.valueOf(damage));	}

	/** Alter max health per level. */
	protected void setMaxHealthPerLevel(double maxHealthPerLevel) 
	{	this.maxHealthPerLevel = maxHealthPerLevel;	}

	/** Alter max mana per level. */
	protected void setMaxManaPerLevel(double maxManaPerLevel) 
	{	this.maxManaPerLevel = maxManaPerLevel;	}

	/** Alter Max Level. */
	protected void setMaxLevel(int maxLevel) 
	{	this.maxLevel = maxLevel;	}

	/** Alter Base Projectile Damage */
	protected void setProjectileDamage(CharacterDamageManager.ProjectileType type, int damage) 
	{	this.projectileDamage.put(type, Integer.valueOf(damage));	}

	/** Alter Scaling Projectile Damage Factor. */
	protected void setProjDamageLevel(CharacterDamageManager.ProjectileType type, double damage)
	{	this.projDamageLevel.put(type, Double.valueOf(damage));	}

	/** Set what Specialized Classes exist for this AlfClass. */
	protected void setSpecializations(Set<AlfClass> specializations) 
	{	this.specializations = specializations;	}

	public String toString()
	{	return this.name;	}

	/** Determine whether this class is a Wildcard. */
	public boolean isWildcardClass()
	{	return this.wildcard;	}

	/** Set whether or not this class is a Wildcard. */
	protected void setWildcardClass(boolean wildcard) 
	{	this.wildcard = wildcard;	}

	/** Add a recipe to this Class. */
	void addRecipe(RecipeGroup rg)
	{	this.recipes.add(rg);	}

	/** Get Mana Color. */
	public int getManaColor()
	{	return this.manaColor;	}

	/** Change Mana Color. */
	void setManaColor(int color) 
	{	this.manaColor = color;	}

	/** Return Mana Regen rate. */
	public double getManaRegen() 
	{	return this.manaRegen;	}

	/** Change Mana Regen. */
	protected void setManaRegen(double manaRegen)
	{	this.manaRegen = manaRegen;	}

	/** Get ManaRegenRate per Level */
	public double getManaRegenPerLevel()
	{	return this.manaRegenPerLevel;	}

	/** Change ManaRegen rate per Level */
	protected void setManaRegenPerLevel(double manaRegenPerLevel)
	{	this.manaRegenPerLevel = manaRegenPerLevel;	}

	@Override
	public int compareTo(AlfClass other) {
		return this.name.compareTo(other.name);
	}
	
}
