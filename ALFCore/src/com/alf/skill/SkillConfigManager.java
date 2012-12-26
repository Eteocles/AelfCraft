package com.alf.skill;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;
import com.alf.chararacter.classes.AlfClass;
import com.alf.util.Setting;
import com.alf.util.Util;

/**
 * Handles external skill configuration.
 * @author Eteocles
 */
public class SkillConfigManager {

	public static Configuration outsourcedSkillConfig;
	public static Configuration standardSkillConfig;
	public static Configuration defaultSkillConfig = new MemoryConfiguration();
	public static File dataFolder;
	public static Map<String, Configuration> classSkillConfigs = new HashMap<String, Configuration>();
	public static File skillConfigFile;
	public static File outsourcedSkillConfigFile;

	/**
	 * Constructs the Skill Config Manager.
	 * @param plugin
	 */
	public SkillConfigManager(AlfCore plugin) {
		dataFolder = plugin.getDataFolder();
		skillConfigFile = new File(dataFolder, "skills.yml");
		outsourcedSkillConfigFile = new File(dataFolder, "permission-skills.yml");
		plugin.getConfigManager().checkForConfig(outsourcedSkillConfigFile);
	}

	/**
	 * Reload the skill config manager.
	 */
	public void reload() {
		standardSkillConfig = null;
		outsourcedSkillConfig = null;
		load();
	}

	/**
	 * Load the skill config manager.
	 */
	public void load() {
		standardSkillConfig = YamlConfiguration.loadConfiguration(skillConfigFile);
		standardSkillConfig.setDefaults(defaultSkillConfig);
		standardSkillConfig.options().copyDefaults(true);

		outsourcedSkillConfig = YamlConfiguration.loadConfiguration(outsourcedSkillConfigFile);
		outsourcedSkillConfig.setDefaults(standardSkillConfig);

		for (String key : standardSkillConfig.getKeys(true))
			if (! standardSkillConfig.isConfigurationSection(key))
				outsourcedSkillConfig.set(key, standardSkillConfig.get(key));
	}

	/**
	 * Save the skill config.
	 */
	public static void saveSkillConfig() {
		try {
			((YamlConfiguration) standardSkillConfig).save(skillConfigFile);
		} catch (IOException e) {
			AlfCore.log(Level.WARNING, "Unable to save default skills file!");
		}
	}

	/**
	 * Get the specific class config.
	 * @param name
	 * @return
	 */
	public Configuration getClassConfig(String name) {
		return (Configuration)classSkillConfigs.get(name);
	}

	/**
	 * Add the configuration settings for a class's skill type.
	 * @param className
	 * @param skillName
	 * @param section
	 */
	public void addClassSkillSettings(String className, String skillName, ConfigurationSection section) {
		Configuration config = (Configuration)classSkillConfigs.get(className);
		if (config == null) {
			config = new MemoryConfiguration();
			classSkillConfigs.put(className, config);
		}
		if (section == null)
			return;
		ConfigurationSection classSection = config.getConfigurationSection(skillName);
		if (classSection == null)
			classSection = config.createSection(skillName);
		for (String key : section.getKeys(true)) {
			if (section.isConfigurationSection(key))
				classSection.createSection(key);
		}
		for (String key : section.getKeys(true))
			if (!section.isConfigurationSection(key))
				classSection.set(key, section.get(key));
	}

	/**
	 * Load defaults for a given skill.
	 * @param skill
	 */
	@SuppressWarnings("unchecked")
	public void loadSkillDefaults(Skill skill) {
		if (! (skill instanceof OutsourcedSkill)) {
			//From the default config in the skill, copy over and create the config
			//sections in the loaded new section.
			ConfigurationSection dSection = skill.getDefaultConfig();
			ConfigurationSection newSection = defaultSkillConfig.createSection(skill.getName());

			for (String key : dSection.getKeys(true)) {
				if (dSection.isConfigurationSection(key))
					newSection.createSection(key);
			}
			for (String key : dSection.getKeys(true))
				if (!dSection.isConfigurationSection(key))
				{
					Object o = dSection.get(key);
					if (o instanceof List)
						newSection.set(key, new ArrayList<String>((List<String>)o));
					else
						newSection.set(key, o);
				}
		}
	}

	/**
	 * Set class configuration 
	 */
	public static void setClassDefaults() {
		for (Configuration config : classSkillConfigs.values())
			config.setDefaults(outsourcedSkillConfig);
	}

	/**
	 * Get the raw string setting for the given setting.
	 * @param skill
	 * @param setting
	 * @param def
	 * @return
	 */
	public static String getRaw(Skill skill, String setting, String def)
	{
		return outsourcedSkillConfig.getString(skill.getName() + "." + setting, def);
	}

	public static String getRaw(Skill skill, Setting setting, String def)
	{
		return getRaw(skill, setting.node(), def);
	}

	public static Boolean getRaw(Skill skill, Setting setting, boolean def)
	{
		return getRaw(skill, setting.node(), def);
	}

	/**
	 * Get the raw boolean setting for the given setting.
	 * @param skill
	 * @param setting
	 * @param def
	 * @return
	 */
	public static Boolean getRaw(Skill skill, String setting, boolean def)
	{
		return Boolean.valueOf(outsourcedSkillConfig.getString(skill.getName() + "." + setting));
	}

	/**
	 * Get the raw keys for a given Skill's setting in the config.
	 * @param skill
	 * @param setting
	 * @return
	 */
	public static Set<String> getRawKeys(Skill skill, String setting) {
		String path = skill.getName();
		if (setting != null)
			path += "." + setting;
		if (! outsourcedSkillConfig.isConfigurationSection(path))
			return new HashSet<String>();

		return outsourcedSkillConfig.getConfigurationSection(path).getKeys(false);
	}

	/**
	 * Get the level required to use a skill by a given alf.
	 * @param alf
	 * @param skill
	 * @param def
	 * @return
	 */
	public static int getLevel(Alf alf, Skill skill, int def) {
		String name = skill.getName();
		if (alf == null)
			return outsourcedSkillConfig.getInt(name + "." + Setting.LEVEL.node(), def);
		int val1 = -1;
		int val2 = -1;
		if (alf.getAlfClass().hasSkill(name))
			val1 = getSetting(alf.getAlfClass(), skill, Setting.LEVEL.node(), def);
		if (alf.getSecondClass() != null && alf.getSecondClass().hasSkill(name))
			val2 = getSetting(alf.getSecondClass(), skill, Setting.LEVEL.node(), def);
		
		if (val1 != -1 && val2 != -1)
			return val1 < val2 ? val1 : val2;
		if (val1 != -1)
			return val1;
		if (val2 != -1)
			return val2;
		
		return outsourcedSkillConfig.getInt(name + "." + Setting.LEVEL.node(), def);
	}

	/**
	 * Get the Object type setting for Skill's node.
	 * @param ac
	 * @param skill
	 * @param setting
	 * @return
	 */
	public static Object getSetting(AlfClass ac, Skill skill, String setting) {
		Configuration config = (Configuration) classSkillConfigs.get(ac.getName());
		if (config == null || ! config.isConfigurationSection(skill.getName()))
			return null;
		return config.get(skill.getName() + "." + setting);
	}

	/**
	 * Get the int type setting for the Skill's node.
	 * @param ac
	 * @param skill
	 * @param setting
	 * @param def
	 * @return
	 */
	public static int getSetting(AlfClass ac, Skill skill, String setting, int def) {
		Object val = getSetting(ac, skill, setting);
		if (val == null)
			return def;
		Integer i = Util.toInt(val);
		return (i != null) ? i : def;
	}

	/**
	 * Get the double type setting for the Skill's node.
	 * @param ac
	 * @param skill
	 * @param setting
	 * @param def
	 * @return
	 */
	public static double getSetting(AlfClass ac, Skill skill, String setting, double def) {
		Object val = getSetting(ac, skill, setting);
		if (val == null)
			return def;
		Double d = Util.toDouble(val);
		return (d != null) ? d : def;
	}

	/**
	 * Get the String type setting for the Skill's node.
	 * @param ac
	 * @param skill
	 * @param setting
	 * @param def
	 * @return
	 */
	public static String getSetting(AlfClass ac, Skill skill, String setting, String def) {
		Object val = getSetting(ac, skill, setting);
		if (val == null)
			return def;
		return val.toString();
	}

	/**
	 * Get the Boolean type setting for the Skill's node.
	 * @param ac
	 * @param skill
	 * @param setting
	 * @param def
	 * @return
	 */
	public static Boolean getSetting(AlfClass ac, Skill skill, String setting, boolean def) {
		Object val = getSetting(ac, skill, setting);
		if (val == null)
			return null;
		if (val instanceof Boolean)
			return (Boolean) val;
		if (val instanceof String)
			return Boolean.valueOf((String) val);
		return null;
	}

	/**
	 * Get the String List type setting for this Skill's node.
	 * @param ac
	 * @param skill
	 * @param setting
	 * @param def
	 * @return
	 */
	public static List<String> getSetting(AlfClass ac, Skill skill, String setting, 
			List<String> def) {
		Configuration config = (Configuration)classSkillConfigs.get(ac.getName());
		if (config == null || !config.isConfigurationSection(skill.getName()))
			return def;
		List<String> val = config.getStringList(skill.getName() + "." + setting);
		return (val != null) && (!val.isEmpty()) ? val : def;
	}
	
	/**
	 * Get the Set of keys for the Setting nodes.
	 * @param ac
	 * @param skill
	 * @param setting
	 * @return
	 */
	public static Set<String> getSettingKeys(AlfClass ac, Skill skill, String setting) {
		String path = skill.getName();
		if (setting != null)
			path += "." + setting;
		Configuration config = (Configuration) classSkillConfigs.get(ac.getName());
		if (config == null || ! config.isConfigurationSection(skill.getName()))
			return new HashSet<String>();
		return config.getConfigurationSection(path).getKeys(false);
	}

	public static int getUseSetting(Alf alf, Skill skill, Setting setting, int def, boolean lower) {
		if (setting == Setting.LEVEL)
			return getLevel(alf, skill, def);
		return getUseSetting(alf, skill, setting.node(), def, lower);
	}

	public static String getUseSetting(Alf alf, Skill skill, Setting setting, String def) 
	{    return getUseSetting(alf, skill, setting.node(), def);	}

	public static double getUseSetting(Alf alf, Skill skill, Setting setting, double def, boolean lower) 
	{    return getUseSetting(alf, skill, setting.node(), def, lower);	}

	public static boolean getUseSetting(Alf alf, Skill skill, Setting setting, boolean def) 
	{    return getUseSetting(alf, skill, setting.node(), def);  }

	public static int getUseSetting(Alf alf, Skill skill, String setting, int def, boolean lower) {
		if (setting.equalsIgnoreCase("level"))
			throw new IllegalArgumentException("Do not use getSetting() for grabbing a alf level!");
		String name = skill.getName();
		if (alf == null)
			return outsourcedSkillConfig.getInt(name + "." + setting, def);
		int val1 = -1;
		int val2 = -1;
		if (alf.canPrimaryUseSkill(skill))
			val1 = getSetting(alf.getAlfClass(), skill, setting, def);
		if (alf.canSecondUseSkill(skill))
			val2 = getSetting(alf.getSecondClass(), skill, setting, def);

		if ((val1 != -1) && (val2 != -1)) {
			if (lower)
				return val1 < val2 ? val1 : val2;
			return val1 > val2 ? val1 : val2;
		} 
		if (val1 != -1)
			return val1;
		if (val2 != -1) {
			return val2;
		}
		return outsourcedSkillConfig.getInt(name + "." + setting, def);
	}

	public static double getUseSetting(Alf alf, Skill skill, String setting, double def, boolean lower) {
		String name = skill.getName();
		if (alf == null)
			return outsourcedSkillConfig.getDouble(name + "." + setting, def);
		double val1 = -1.0D;
		double val2 = -1.0D;
		if (alf.canPrimaryUseSkill(skill))
			val1 = getSetting(alf.getAlfClass(), skill, setting, def);
		if (alf.canSecondUseSkill(skill))
			val2 = getSetting(alf.getSecondClass(), skill, setting, def);

		if ((val1 != -1.0D) && (val2 != -1.0D)) {
			if (lower)
				return val1 < val2 ? val1 : val2;
			return val1 > val2 ? val1 : val2;
		}
		if (val1 != -1.0D)
			return val1;
		if (val2 != -1.0D) {
			return val2;
		}
		return outsourcedSkillConfig.getDouble(name + "." + setting, def);
	}

	public static boolean getUseSetting(Alf alf, Skill skill, String setting, boolean def) {
		if (alf == null) {
			return outsourcedSkillConfig.getBoolean(skill.getName() + "." + setting, def);
		}
		Boolean val1 = null;
		Boolean val2 = null;

		if (alf.canPrimaryUseSkill(skill))
			val1 = getSetting(alf.getAlfClass(), skill, setting, def);

		if (alf.canSecondUseSkill(skill))
			val2 = getSetting(alf.getSecondClass(), skill, setting, def);

		if ((val1 == null) && (val2 == null))
			return def;
		if ((val2 != null) && (val2 != null))
			return val2;
		return val1;
	}

	public static String getUseSetting(Alf alf, Skill skill, String setting, String def)
	{
		if (alf == null)
			return outsourcedSkillConfig.getString(skill.getName() + "." + setting, def);
		if (alf.canPrimaryUseSkill(skill))
			return getSetting(alf.getAlfClass(), skill, setting, def);
		if (alf.canSecondUseSkill(skill))
			return getSetting(alf.getSecondClass(), skill, setting, def);
		return outsourcedSkillConfig.getString(skill.getName() + "." + setting, def);
	}

	public static List<String> getUseSetting(Alf alf, Skill skill, String setting, List<String> def) {
		if (alf == null) {
			List<String> list = outsourcedSkillConfig.getStringList(skill.getName() + "." + setting);
			return list != null ? list : def;
		}

		List<String> vals = new ArrayList<String>();
		if (alf.canPrimaryUseSkill(skill)) {
			List<String> list = getSetting(alf.getAlfClass(), skill, setting, new ArrayList<String>());
			vals.addAll(list);
		}
		if (alf.canSecondUseSkill(skill)) {
			List<String> list = getSetting(alf.getSecondClass(), skill, setting, new ArrayList<String>());
			vals.addAll(list);
		}
		if (!vals.isEmpty())
			return vals;
		List<String> list = outsourcedSkillConfig.getStringList(skill.getName() + "." + setting);
		return (list != null) && (!list.isEmpty()) ? list : def;
	}

}
