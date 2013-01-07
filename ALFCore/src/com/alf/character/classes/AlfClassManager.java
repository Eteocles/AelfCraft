package com.alf.character.classes;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.alf.AlfCore;
import com.alf.character.CharacterDamageManager;
import com.alf.skill.OutsourcedSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.util.Properties;
import com.alf.util.RecipeGroup;
import com.alf.util.Util;

/**
 * Manages all classes.
 * @author Eteocles
 */
public class AlfClassManager {

	private final AlfCore plugin;
	private Set<AlfClass> classes;
	private AlfClass defaultClass;
	private HashMap<AlfClass, Set<String>> weakParents = new HashMap<AlfClass, Set<String>>();
	private HashMap<AlfClass, Set<String>> strongParents = new HashMap<AlfClass, Set<String>>();

	/**
	 * Construct the class manager.
	 * @param plugin
	 */
	public AlfClassManager(AlfCore plugin) {
		this.plugin = plugin;
		this.classes = new TreeSet<AlfClass>();
	}

	/** Add a class to the class manager. */
	public boolean addClass(AlfClass c)
	{	return this.classes.add(c);	}

	/** Return the class corresponding to the input string, or null if none exists. */
	public AlfClass getClass(String name) {
		for (AlfClass c : this.classes)
			if (name.equalsIgnoreCase(c.getName()))
				return c;
		return null;
	}

	/** Return set of all classes. */
	public Set<AlfClass> getClasses()
	{	return this.classes;	}

	/** Return the Default class. */
	public AlfClass getDefaultClass()
	{	return this.defaultClass;	}

	/** Remove the class from the manager. */
	public boolean removeClass(AlfClass c)
	{	return this.classes.remove(c);	}

	/** Set the default class for the manager. */
	public void setDefaultClass(AlfClass defaultClass)
	{	this.defaultClass = defaultClass;	}

	/** Generate permissions for all classes. */
	private void registerClassPermissions() {
		Map<String, Boolean> classPermissions = new HashMap<String, Boolean>();
		for (AlfClass alfClass : this.classes) {
			if (alfClass.isWildcardClass()) {
				Permission p = new Permission("AlfCore.classes." + alfClass.getName().toLowerCase(), PermissionDefault.OP);
				Bukkit.getServer().getPluginManager().addPermission(p);
				classPermissions.put("AlfCore.classes."+ alfClass.getName().toLowerCase(), true);
			} else {
				Permission p = new Permission("AlfCore.classes." + alfClass.getName().toLowerCase(), PermissionDefault.OP);
				Bukkit.getServer().getPluginManager().addPermission(p);
			}
		}
		Permission wildcardClassPermission = new Permission("AlfCore.classes.*", "Grants access to all classes.",
				PermissionDefault.OP, classPermissions);
		this.plugin.getServer().getPluginManager().addPermission(wildcardClassPermission);
	}

	/** Determine class links. */
	private void checkClassHierarchy() {
		AlfClass unlinkedClass;

		for (Iterator<AlfClass> it = this.classes.iterator(); it.hasNext(); ) {
			unlinkedClass = (AlfClass) it.next();
			Set<String> strong = (Set<String>) this.strongParents.get(unlinkedClass);
			if (strong != null && ! strong.isEmpty()) {
				for (String sp : strong) {
					AlfClass parent = getClass(sp);
					if (parent != null)
						try {
							unlinkedClass.addStrongParent(parent);
							parent.addSpecialization(unlinkedClass);
						} catch (AlfClass.CircularParentException e) {
							AlfCore.log(Level.SEVERE, "Cannot assign " + unlinkedClass.getName() + 
									" a parent class as " + sp + " is already a parent of that class.");
						}
					else
						AlfCore.log(Level.WARNING, "Cannot assign " + unlinkedClass.getName() + 
								" a parent class as " + sp + " does not exist.");
				}
			} 
			Set<String> weak = (Set<String>) this.weakParents.get(unlinkedClass);
			if (weak != null && ! weak.isEmpty())
				for (String wp : weak) {
					AlfClass parent = getClass(wp);
					if (parent != null)
						try {
							unlinkedClass.addWeakParent(parent);
							parent.addSpecialization(unlinkedClass);
						} catch (AlfClass.CircularParentException e) {
							AlfCore.log(Level.WARNING, "Cannot assign " + unlinkedClass.getName() + " a parent class as " +
									wp + " does not exist.");
						}
					else
						AlfCore.log(Level.WARNING, "Cannot assign " + unlinkedClass.getName() + 
								" a parent class as " + wp + " does not exist.");
				}
		}

		this.strongParents.clear();
		this.strongParents = null;
		this.weakParents.clear();
		this.weakParents = null;
	}

	/** Load all of the classes and store them from an input config file. */
	public boolean loadClasses(File file) {
		if (file.listFiles().length == 0) {
			AlfCore.log(Level.WARNING, "You have no classes defined in your setup! Disabling.");
			return false;
		}

		for (File f : file.listFiles()) {
			if (f.isFile() && f.getName().contains(".yml")) {
				AlfClass newClass = loadClass(f);
				if (newClass == null)
					AlfCore.log(Level.WARNING, "Attempted to load " + f.getName() + " but failed. Skipping.");
				else if (! addClass(newClass))
					AlfCore.log(Level.WARNING, "Dupicate class (" + newClass.getName() + ") found. Skipping this class.");
				else AlfCore.log(Level.INFO, "Loaded class: " + newClass.getName());
			}
		}

		checkClassHierarchy();
		SkillConfigManager.saveSkillConfig();
		SkillConfigManager.setClassDefaults();

		if (this.defaultClass == null) {
			AlfCore.log(Level.SEVERE, "You are missing a default class! Disabling...");
			return false;
		}

		if (this.plugin.getServer().getPluginManager().getPermission("AlfCore.classes.*") == null)
			registerClassPermissions();

		return true;
	}

	/**
	 * Load in class information from its File.
	 * @param file
	 * @return
	 */
	private AlfClass loadClass(File file) {
		Configuration config = YamlConfiguration.loadConfiguration(file);
		String className = config.getString("name");
		if (className == null)
			return null;

		AlfClass newClass = new AlfClass(className, this.plugin);

		newClass.setDescription(config.getString("description", ""));
		newClass.setExpModifier(config.getDouble("expmodifier", 1.0D));
		newClass.setPrimary(config.getBoolean("primary", true));
		newClass.setSecondary(config.getBoolean("secondary", false));
		newClass.setTier(config.getInt("tier", 1));

		if (newClass.getTier() < 0)
			newClass.setTier(0);

		//Load in other stuff...
		loadArmor(newClass, config.getStringList("permitted-armor"));
		loadWeapons(newClass, config.getStringList("permitted-weapon"));
		loadDamages(newClass, config);
		loadPermittedSkills(newClass, config.getConfigurationSection("permitted-skills"));
		loadPermissionSkills(newClass, config.getConfigurationSection("permission-skills"));
		loadExperienceTypes(newClass, config.getStringList("experience-sources"));

		newClass.setWildcardClass(config.getBoolean("wildcard-permission", true));
		newClass.setBaseMaxHealth(config.getInt("base-max-health", 20));

		//Invalid base health.
		if (newClass.getBaseMaxHealth() <= 0) 
			AlfCore.log(Level.SEVERE,"Invalid base health defined for: "+newClass.getName()+" please set higher than 0");

		newClass.setMaxHealthPerLevel(config.getDouble("max-health-per-level", 0.0D));
		newClass.setBaseMaxMana(config.getInt("base-max-mana", 100));
		newClass.setMaxManaPerLevel(config.getDouble("max-mana-per-level", 0.0D));
		newClass.setManaRegen(config.getDouble("mana-regen", 1.0D));
		newClass.setManaRegenPerLevel(config.getDouble("mana-regen-per-level", 0.0D));

		//		if (AlfCore.useSpout())
		//			newClass.setManaColor(config.getInt("mana-bar-color", 52479));

		if (config.isSet("recipes")) {
			for (String s : config.getStringList("recipes")) {
				RecipeGroup rg = (RecipeGroup)AlfCore.properties.recipes.get(s.toLowerCase());
				if (rg == null)
					AlfCore.log(Level.SEVERE, "No recipe group named "+s+" defined in recipes.yml. Check "+className+" for errors or add the recipe group!");
				else
					newClass.addRecipe(rg);
			}
		} else AlfCore.log(Level.SEVERE, "Class "+className+" has no recipes set! They will not be able to craft items!");

		newClass.setExpLoss(config.getDouble("expLoss", -1.0D));
		newClass.setPvpExpLoss(config.getDouble("pvpExpLoss", -1.0D));

		int defaultMaxLevel = Properties.maxLevel;
		int maxLevel = config.getInt("max-level", defaultMaxLevel);
		if (maxLevel < 1) {
			AlfCore.log(Level.WARNING, "Class ("+className+") max level is too low. Setting max level to 1.");
			maxLevel = 1;
		} else if (maxLevel > defaultMaxLevel) {
			AlfCore.log(Level.WARNING, "Class ("+className+") max level is too high. Setting max level to "+defaultMaxLevel+".");
			maxLevel = defaultMaxLevel;
		}
		newClass.setMaxLevel(maxLevel);

		double defaultCost = 0.0D;
		if (newClass.isPrimary())
			defaultCost = AlfCore.properties.swapCost;
		else
			defaultCost = AlfCore.properties.profSwapCost;

		double cost = config.getDouble("cost", defaultCost);
		if (cost < 0.0D) {
			AlfCore.log(Level.WARNING, "Class ("+className+") cost is too low. Setting cost to 0.");
			cost = 0.0D;
		}
		newClass.setCost(cost);

		Set<String> strongParents = new HashSet<String>();
		if (config.isConfigurationSection("parents")) {
			List<String> list = config.getStringList("parents.strong");
			if (list != null) {
				strongParents.addAll(list);
			}

			list = config.getStringList("parents.weak");
			Set<String> weakParents = new HashSet<String>();
			if (list != null) {
				weakParents.addAll(list);
			}

			this.weakParents.put(newClass, weakParents);
			this.strongParents.put(newClass, strongParents);
		}

		if (config.getBoolean("default", false)) {
			AlfCore.debugLog(Level.INFO, "Default class found: "+className);
			this.defaultClass = newClass;
		}
		//		if (AlfCore.useSMS) {
		//			MenuHandler.setupMenu(newClass, this.plugin);
		//		}

		return newClass;
	}

	/**
	 * Load experience type information.
	 * @param newClass
	 * @param experienceNames
	 */
	private void loadExperienceTypes(AlfClass newClass, List<String> experienceNames) {
		String className = newClass.getName();
		Set<AlfClass.ExperienceType> experienceSources = EnumSet.noneOf(AlfClass.ExperienceType.class);

		if (experienceNames == null || experienceNames.isEmpty())
			AlfCore.log(Level.WARNING, className+" has no experience-sources section");
		else {
			for (String experience : experienceNames)
				try {
					boolean added = experienceSources.add(AlfClass.ExperienceType.valueOf(experience.toUpperCase()));
					if (!added)
						AlfCore.log(Level.WARNING, "Duplicate experience source ("+experience+") defined for "+className+".");
				}
			catch (IllegalArgumentException e) {
				AlfCore.log(Level.WARNING, "Invalid experience source ("+experience+") defined for "+className+". Skipping this source.");
			}
		}
		newClass.setExperienceSources(experienceSources);
	}

	/**
	 * Load permission skills for the class.
	 * @param newClass
	 * @param section
	 */
	private void loadPermissionSkills(AlfClass newClass,
			ConfigurationSection section) {
		if (section != null) {
			String className = newClass.getName();

			Set<String> permissionSkillNames = section.getKeys(false);
			if (permissionSkillNames != null)
				for (String skill : permissionSkillNames) {
					if (newClass.hasSkill(skill))
						AlfCore.log(Level.WARNING, "Skill already assigned ("+skill+") for "+className+". Skipping this skill");
					else try {
						if (this.plugin.getSkillManager().isLoaded(skill) || 
								this.plugin.getSkillManager().loadOutsourcedSkill(skill)) {
							newClass.addSkill(skill);
							ConfigurationSection skillSettings = section.getConfigurationSection(skill);
							if (skillSettings == null)
								skillSettings = section.createSection(skill);
							this.plugin.getSkillConfigs().addClassSkillSettings(className, this.plugin.getSkillManager().getSkill(skill).getName(), skillSettings);
						}
					} catch (IllegalArgumentException e) {
						AlfCore.log(Level.WARNING, "Invalid permission skill ("+skill+") defined for "+className+". Skipping this skill.");
					}
				}
		}
	}

	/**
	 * Load permitted skills for the class.
	 * @param newClass
	 * @param section
	 */
	private void loadPermittedSkills(AlfClass newClass, ConfigurationSection section) {
		//Whatever, saves me a lot of indentation space.
		if (section == null)
			return;
		
		String className = newClass.getName();
		Set<String> skillNames = section.getKeys(false);

		if (skillNames.isEmpty())
			AlfCore.log(Level.WARNING, className+" has no permitted-skills section");
		else {
			boolean allSkills = false;
			for (String skillName : skillNames) {
				if (skillName.equals("*") || skillName.toLowerCase().equals("all"))
					allSkills = true;
				else {
					Skill skill = this.plugin.getSkillManager().getSkill(skillName);
					if (skill == null)
						AlfCore.log(Level.WARNING, "Skill "+skillName+" defined for "+className+" not found.");
					else {
						newClass.addSkill(skillName);

						ConfigurationSection skillSettings = section.getConfigurationSection(skillName);
						if (skillSettings == null)
							skillSettings = section.createSection(skillName);
						this.plugin.getSkillConfigs().addClassSkillSettings(className, skill.getName(), skillSettings);
					}
				}
			}
			if (allSkills) {
				this.plugin.getSkillManager().loadSkills();
				for (Skill skill : this.plugin.getSkillManager().getSkills()) {
					if (!newClass.hasSkill(skill.getName()) && !(skill instanceof OutsourcedSkill)) {
						newClass.addSkill(skill.getName());

						ConfigurationSection skillSettings = section.getConfigurationSection(skill.getName());

						if (skillSettings == null)
							skillSettings = section.createSection(skill.getName());

						this.plugin.getSkillConfigs().addClassSkillSettings(newClass.getName(), skill.getName(), skillSettings);
					}
				}
			}
		}
	}

	/**
	 * Load damage information.
	 * @param newClass
	 * @param config
	 */
	private void loadDamages(AlfClass newClass, Configuration config) {
		String className = newClass.getName();
		//Item Base Damage
		ConfigurationSection section;

		//Projectile Base Damage
		section = config.getConfigurationSection("projectile-damage");
		if (section != null) {
			Set<String> projectileDamages = section.getKeys(false);
			if (projectileDamages == null || projectileDamages.isEmpty())
				AlfCore.log(Level.WARNING, className+" has no projectile damage section");
			else {
				for (String projectileName : projectileDamages) {
					CharacterDamageManager.ProjectileType type = CharacterDamageManager.ProjectileType.matchProjectile(projectileName);
					if (type != null && (section.get(projectileName) instanceof Number)) {
						int damage = section.getInt(projectileName);
						newClass.setProjectileDamage(type, damage);
					} else {
						AlfCore.log(Level.WARNING, "Invalid projectile-damage type or value for ("+projectileName+") defined in "+className);
					}
				}
			}

		}

		//Projectile Damage by Level
		section = config.getConfigurationSection("projectile-damage-level");
		if (section != null) {
			Set<String> projectileDamages = section.getKeys(false);
			if (projectileDamages == null || projectileDamages.isEmpty())
				AlfCore.log(Level.WARNING, className+" has no projectile damage section");
			else
				for (String projectileName : projectileDamages) {
					CharacterDamageManager.ProjectileType type = CharacterDamageManager.ProjectileType.matchProjectile(projectileName);
					if (type != null && (section.get(projectileName) instanceof Number)) {
						double damage = section.getDouble(projectileName);
						newClass.setProjDamageLevel(type, damage);
					} else {
						AlfCore.log(Level.WARNING, "Invalid projectile-damage-level type or value for ("+projectileName+") defined in "+className);
					}
				}
		}
	}

	/**
	 * Load weapon information.
	 * @param newClass
	 * @param weapons
	 */
	private void loadWeapons(AlfClass newClass, List<String> weapons) {
		String wLimits = "", className = newClass.getName();

		if (weapons == null || weapons.isEmpty()) {
			AlfCore.log(Level.WARNING, className + " has no permitted-weapon section.");
			return;
		}

		for (String w : weapons) {
			boolean matched = false;
			for (String s : Util.weapons) {
				if (w.equals("*") || w.equalsIgnoreCase("ALL")) {
					newClass.addAllowedWeapon(Material.matchMaterial(s));
					wLimits += " " + s;
					matched = true;
				} else if (s.contains(w.toUpperCase())) {
					if (! s.contains("PICK") || w.contains("PICK") || ! w.contains("AXE")) {
						newClass.addAllowedWeapon(Material.matchMaterial(s));
						wLimits += " " + s;
						matched = true;
					}
				}
			}
			if (w.equals("*") || w.equals("ALL"))
				break;
			if (! matched)
				AlfCore.log(Level.WARNING, "Invalid weapon type (" + w + ") defined for " + className);
		}
		AlfCore.debugLog(Level.INFO, "Allowed weapons - " + wLimits);
	}

	/**
	 * Load armor information.
	 * @param newClass
	 * @param armors
	 */
	private void loadArmor(AlfClass newClass, List<String> armors) {
		String aLimits = "", className = newClass.getName();

		if (armors == null || armors.isEmpty()) {
			AlfCore.log(Level.WARNING, className+" has no permitted-armor section");
			return;
		}
		for (String a : armors) {
			boolean matched = false;
			for (String s : Util.armors)
				if (s.contains(a.toUpperCase()) || a.equals("*") || a.equalsIgnoreCase("ALL")) {
					newClass.addAllowedArmor(Material.matchMaterial(s));
					aLimits+=" "+s;
					matched = true;
				}

			if (a.equals("*") || a.equals("ALL"))
				break;
			if (!matched)
				AlfCore.log(Level.WARNING, "Invalid armor type ("+a+") defined for "+className);
		}
		AlfCore.debugLog(Level.INFO, "Allowed Armor - "+aLimits.toString());
	}
}
