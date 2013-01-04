package com.alf.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.classes.AlfClassManager;
import com.alf.skill.SkillConfigManager;

/**
 * Handles all external configuration.
 * @author Eteocles
 */
public class ConfigManager {

	protected final AlfCore plugin;
	protected static File classConfigFolder;
	protected static File expConfigFile;
	protected static File damageConfigFile;
	protected static File recipesConfigFile;
	private static Configuration damageConfig;
	private static Configuration expConfig;
	private static Configuration recipeConfig;

	/**
	 * Construct the ConfigManager.
	 * @param plugin
	 */
	public ConfigManager(AlfCore plugin) {
		this.plugin = plugin;
		File dataFolder = plugin.getDataFolder();
		classConfigFolder = new File(dataFolder+File.separator + "classes");
		expConfigFile = new File(dataFolder, "experience.yml");
		damageConfigFile = new File(dataFolder, "damages.yml");
		recipesConfigFile = new File(dataFolder, "recipes.yml");
	}

	/**
	 * Load the config manager.
	 * @throws Exception
	 */
	public void load() throws Exception {
		//Check for external config files.
		checkForConfig(expConfigFile);
		checkForConfig(damageConfigFile);
		checkForConfig(recipesConfigFile);
		//Check for class configuration folder.
		if (! classConfigFolder.exists()) {
			classConfigFolder.mkdirs();
			checkForConfig(new File(classConfigFolder, "citizen.yml"));
			checkForConfig(new File(classConfigFolder, "warrior.yml"));
			//TODO Add other class files.
		}
		//Check for Skill config files.
		this.plugin.setSkillConfigs(new SkillConfigManager(this.plugin));
		this.plugin.getSkillConfigs().load();
	}

	/** Initialize configuration. */
	public boolean loadManagers() {
		//Load in damages config.
		damageConfig = YamlConfiguration.loadConfiguration(damageConfigFile);
		InputStream defConfigStream = this.plugin.getResource("defaults" + File.separator + "damages.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			damageConfig.setDefaults(defConfig);
		}
		this.plugin.getDamageManager().load(damageConfig);

		//Load in experience config.
		expConfig = YamlConfiguration.loadConfiguration(expConfigFile);
		defConfigStream = this.plugin.getResource("defaults" + File.separator + "experience.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			expConfig.setDefaults(defConfig);
		}
		loadExperience();

		//Load in recipe config.
		recipeConfig = YamlConfiguration.loadConfiguration(recipesConfigFile);
		loadRecipes();

		//Load in class config.
		AlfClassManager alfClassManager = new AlfClassManager(this.plugin);
		if (!alfClassManager.loadClasses(classConfigFolder))
			return false;
		this.plugin.setClassManager(alfClassManager);
		return true;
	}

	/** Reload configuration. */
	public boolean reload() {
		try {
			this.plugin.getCharacterManager().shutdown();
			this.plugin.getSkillConfigs().reload();
			damageConfig = null;
			expConfig = null;
			recipeConfig = null;
			this.plugin.setClassManager(null);
			//Reload managers.
			loadManagers();
			//Check all players.
			for (Player player : this.plugin.getServer().getOnlinePlayers()) {
				Alf alf = this.plugin.getCharacterManager().getAlf(player);
				this.plugin.getCharacterManager().performSkillChecks(alf);
			}
		} catch (Exception e) { 
			e.printStackTrace();
			AlfCore.log(Level.SEVERE, "Critical error encountered while reloading. Disabling...");
			this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
			return false;
		}
		AlfCore.log(Level.INFO, "Reloaded Configuration.");
		return true;
	}

	/** Check for the given file. If it doesn't exist, generate defaults. */
	public void checkForConfig(File config) {
		if (! config.exists())
			try {
				AlfCore.log(Level.WARNING, "File "+config.getName()+" not found - generating defaults.");
				config.getParentFile().mkdir();
				config.createNewFile();
				OutputStream output = new FileOutputStream(config, false);
				InputStream input = ConfigManager.class.getResourceAsStream("/defaults/"+config.getName());
				byte[] buf = new byte[8192];
				int length = 1;
				while (length >= 0) {
					length = input.read(buf);
					if (length >= 0)
						output.write(buf, 0, length);
				}
				input.close();
				output.close();
			} catch (Exception e) { e.printStackTrace(); }
	}

	/** Load all recipes from config. */
	private void loadRecipes() {
		Set<String> recipes = recipeConfig.getKeys(false);
		if (recipes.isEmpty())
			AlfCore.log(Level.WARNING, "No recipes found!");
		for (String key : recipes) {
			//Load Permitted Recipes.
			int level = recipeConfig.getInt(key + ".level", 1);
			RecipeGroup rg = new RecipeGroup(key, level);
			List<String> items = recipeConfig.getStringList(key + ".items");
			if (items != null && !items.isEmpty()) {
				for (String i : items) {
					String vals[] = i.split(":");
					//All Recipes if First Parameter is * or all.
					if (vals[0].equalsIgnoreCase("*") || vals[0].equalsIgnoreCase("all")) {
						rg.setAllRecipes(true);
						break;
					} try {
						Material mat = Material.getMaterial(Integer.parseInt(vals[0]));
						if (mat == null) {
							AlfCore.log(Level.SEVERE, "Invalid item ID in recipe group "+ key + " ID: " + vals[0] + " is not a block ID!");
							continue;
						}
						short subType = getMinDataVal(mat);
						if (vals.length > 1) {
							//All SubTypes.
							if (vals[1].equals("*")) {
								for (short j = getMinDataVal(mat); j <= getMaxDataVal(mat); j = (short)(j+1))
									rg.put(new ItemData(mat, j), true);
							} else {
								subType = Short.parseShort(vals[1]);
								if (subType < getMinDataVal(mat) || subType > getMaxDataVal(mat)) {
									AlfCore.log(Level.WARNING, "Invalid item subtype in recipe group " + key + " ID: " + vals[0] + " subtype: " + 
											vals[1] + " is below or above min/max values. Default to " + getMinDataVal(mat));
									subType = getMinDataVal(mat);
								}
								rg.put(new ItemData(mat, subType), true);
							}
						} else rg.put(new ItemData(mat, subType), true);
					} catch (NumberFormatException e) { AlfCore.log(Level.SEVERE, "Invalid item ID in recipe group " + key);	}
				}
			}
			//Load Denied Items.
			List<String> deniedItems = recipeConfig.getStringList(key + ".denied-items");
			if (deniedItems != null && ! deniedItems.isEmpty()) {
				for (String i : deniedItems) {
					String[] vals = i.split(":");
					try {
						Material mat = Material.getMaterial(Integer.parseInt(vals[0]));
						if (vals.length > 1) {
							//All SubTypes.
							if (vals[1].equals("*")) {
								for (short j = 0; j < getMaxDataVal(mat); j = (short)(j+1))
									rg.put(new ItemData(mat, j), false);
							} else {
								short subType = Short.parseShort(vals[1]);
								rg.put(new ItemData(mat, subType), false);
							}
						} else rg.put(new ItemData(mat, (short)0), false);
					} catch (NumberFormatException e) { AlfCore.log(Level.SEVERE, "Invalid item ID in recipe group " + key); }
				}
			}
		}
	}

	/** Load experience information from config. */
	private void loadExperience() {
		if (expConfig != null) {
			AlfCore.properties.creatureKillingExp = loadEntityExperience(expConfig.getConfigurationSection("killing"), "killing");
			AlfCore.properties.breedingExp = loadEntityExperience(expConfig.getConfigurationSection("breeding"), "breeding");
			AlfCore.properties.playerKillingExp = expConfig.getDouble("player-killing", 0.0D);
			AlfCore.properties.miningExp = loadMaterialExperience(expConfig.getConfigurationSection("mining"));
			AlfCore.properties.farmingExp = loadMaterialExperience(expConfig.getConfigurationSection("farming"));
			AlfCore.properties.loggingExp = loadMaterialExperience(expConfig.getConfigurationSection("logging"));

			AlfCore.properties.fishingExp = expConfig.getDouble("fishing", 0.0D);
			AlfCore.properties.metalExpMultiplier = expConfig.getDouble("engineering", 0.0D);
			AlfCore.properties.smeltExpMultiplier = expConfig.getDouble("smelting", 0.0D);
			AlfCore.properties.enchantingExp = expConfig.getDouble("enchanting", 0.0D);
			AlfCore.properties.tradingExp = expConfig.getDouble("trading", 0.0D);
		} 
	}

	/**
	 * Load entity type experience.
	 * @param section
	 * @return
	 */
	private Map<EntityType, Double> loadEntityExperience(ConfigurationSection section, String name) {
		Map<EntityType, Double> expMap = new HashMap<EntityType, Double>();
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			boolean errored = false;
			if (keys != null && ! keys.isEmpty()) {
				for (String item : keys)
					try {
						double exp = section.getDouble(item, 0.0D);
						EntityType type = EntityType.fromName(item);
						if (type == null)
							throw new IllegalArgumentException();
						expMap.put(type, exp);
					} catch (IllegalArgumentException e) {
						AlfCore.log(Level.WARNING, "Invalid creature type (" + item + ") found in experience.yml.");
						errored = true;
					}
			} else {
				AlfCore.log(Level.WARNING, "No Experience Section " + name + " defined!");
			}
			if (errored)
				AlfCore.log(Level.WARNING, "Remember, creature names are case sensitive and must be exactly the same as found in the defaults.");
		} else
			AlfCore.log(Level.WARNING, "No Experience Section " + name + " defined!");
		return expMap;
	}

	/** Load map of materials and their corresponding experience gains. */
	private Map<Material, Double> loadMaterialExperience(ConfigurationSection section) {
		Map<Material, Double> expMap = new HashMap<Material, Double>();
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			for (String item : keys) {
				double exp = section.getDouble(item, 0.0D);
				Material type = Material.matchMaterial(item);
				if (type != null)
					expMap.put(type,  exp);
				else {	AlfCore.log(Level.WARNING, "Invalid material type (" + item + ") found in experience.yml.");	}
			}
		}
		return expMap;
	}

	/** Get minimum data value for a given material. */
	private short getMinDataVal(Material mat) {
		return 0;
	}

	/** Get maximum data value for a given material. */
	private short getMaxDataVal(Material mat) {
		switch (mat) {
		case COAL:
			return 1;
		case LONG_GRASS:
			//What else?
			return 2;
		case WOOD:
		case LEAVES:
		case SMOOTH_BRICK:
		case WOOD_STEP:
			return 3;
		case SAPLING:
		case STEP:
			return 5;
		case CROPS:
		case PUMPKIN_STEM:
		case MELON_STEM:
			return 7;
		case INK_SACK:
		case WOOL:
			return 15;
		default:
			return 0;
		}
	}


}
