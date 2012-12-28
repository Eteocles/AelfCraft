package com.alf.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionType;

import com.alf.AlfCore;

/**
 * Contains global plugin properties.
 * @author Eteocles
 */
public class Properties {

	/** Exp leveling curve. (Higher numbers = longer ending levels.) */
	public double power;
	/** Maximum experience value for players. Don't change this after it has been set. */
	public static int maxExp;
	/** The maximum level to master (or to change from a parent to child class). */
	public static int maxLevel;
	/** Allow players to gain experience up until they are 1% away from being above max-level. */
	public static boolean padMaxLevel;
	/** Contains experience quantities corresponding to levels. */
	public static int levels[];
	/** Amount of experience lost on death, taken from current level and not total experience. */
	public double expLoss;
	/** Experience loss multiplier used during PvP. */
	public double pvpExpLossMultiplier = 0.0D;
	/** Allow level loss when players lose experience. */
	public boolean levelsViaExpLoss = false;
	/** Allow loss of mastery when players lose experience. */
	public boolean masteryLoss = false;
	/** Maximum party size. */
	public int maxPartySize = 6;
	/** Multiplier of how much bonus experience should be awarded to parties. */
	public double partyBonus = 0.0D;
	/** Experience gained for killing a player. */
	public double playerKillingExp = 0.0D;
	/** Allow experience adjustments when a mob was spawned from a mob-spawner. */
	public boolean noSpawnCamp = false;
	/** Multiplier for experience adjustments when a mob was spawned from a mob-spawner. */
	public double spawnCampExpMult;
	/** [HARDCORE] Reset all levels/exp on death. */
	public boolean resetOnDeath;
	/** Level that players must be within to PvP with each other. */
	public int pvpLevelRange = 50;
	/** Minimum level a player must attain before they can PvP or be PvPed with. */
	public int minPvpLevel = 1;
//	/**  */
//	public boolean orbExp;
	/** Maximum range when players will receive no experience for killing a player. */
	public int pvpMaxExpRange = 0;
	/** How close in level players need to be to get full experience. */
	public int pvpExpRange = 0;
	/**  Experience party multipliers corresponding to party size.*/
	public static double[] partyMults;
	/** Cost to swap combat spec. */
	public double swapCost;
	/** Cost to swap to an old combat spec. */
	public double oldClassSwapCost;
	/** Cost to swap professions. */
	public double profSwapCost;
	/** Cost curve exponent. (Higher numbers = higher cost tiers) */
	public double profSwapPower;
	/** Whether the first class swap is free. */
	public boolean firstSwitchFree;
	/** Allow users to swap into their mastered class for free. */
	public boolean swapMasterFree;
	/** Enable class-prefixes in chat. */
	public boolean prefixClassName;
	/** Reset experience on class change. */
	public boolean resetExpOnClassChange = true;
	/** Reset all experience including mastery on class change. */
	public boolean resetMasteryOnClassChange = false;
	/** Reset profession mastery on change. */
	public boolean resetProfMasteryOnClassChange = false;
	/** Reset profession experience on primary change. */
	public boolean resetProfOnPrimaryChange = false;
	/** Lock class swaps until mastery. */
	public boolean lockPathTillMaster = false;
	/** Lock class swaps when mastery is attained. */
	public boolean lockAtHighestTier = false;
	/** Debug mode. */
	public boolean debug;
	/** Type of storage to use. */
	public String storageType;
	/** Whether to use economy. */
	public boolean economy;
	/** Amount of time that a block should be tracked for no experience yield. */
	public int blockTrackingDuration;
	/** Maximum amount of blocks to be tracked at any point in time. */
	public int maxTrackedBlocks;
	/** % of total health gained when a player regains health from being full. */
	public double foodHealPercent = 0.05D;
	/** Milliseconds a player must wait before using a second skill. */
	public int globalCooldown;
	/** Experience cost multiplier for enchants. Set to 0 for disabling. */
	public double enchantXPMultiplier;
	/** Slow the skill caster while using a skill with a warmup. */
	public boolean slowCasting = true;
	/** Duration of a combat. (Unless reset) */
	public static int combatTime;
	/** Whether a bed heals a player. */
	public boolean bedHeal;
	/** Period of heal-signals. */
	public int healInterval;
	/** Amplitude of heal-signal. */
	public int healPercent;
	/** Amplitude of mana-signal. */
	public int manaRegenPercent;
	/** Period of heal-signal. */
	public int manaRegenInterval;
	/** Level required to use hats. */
	public int hatsLevel;
	/** Whether to enable hats. */
	public boolean allowHats;
	/** Experience rate during bonus period. */
	public double expBonus;
	/** Time in milliseconds when the bonus will end. */
	public long expiration;
	/** Bonus message. */
	public String bonusMessage;
	/** Maps a name to its corresponding recipe group. */
	public Map<String, RecipeGroup> recipes = new HashMap<String, RecipeGroup>();
	/** Loads in the CharacterDamageManager. Hands amount of potion health per tier. */
	public double potHealthPerTier;
	//Experience
	/** Experience gained from killing entity types. */
	public Map<EntityType, Double> creatureKillingExp = new EnumMap<EntityType, Double>(EntityType.class);
	/** Experience gained from farming certain blocks. */
	public Map<Material, Double> farmingExp = new EnumMap<Material, Double>(Material.class);
	/** Experience gained from breeding animals. */
	public Map<EntityType, Double> breedingExp = new EnumMap<EntityType, Double>(EntityType.class);
	/** Experience gained from expending 1 metal. */
	public double metalExpMultiplier = 0.0D;
	/** Experience gained from smelting 1 item. */
	public double smeltExpMultiplier = 0.0D;
	/** Experience gained from fishing. */
	public double fishingExp = 0.0D;
	/** Experience gained from mining ores. */
	public Map<Material, Double> miningExp = new EnumMap<Material, Double>(Material.class);
	/** Experience gained from brewing. */
	public Map<PotionType, Double> potionExp = new EnumMap<PotionType, Double>(PotionType.class);
	/** Experience gained for enchanting. TODO PLEASE FIX */
	public double enchantingExp = 0.0D;
	/** Experience gained for logging. */
	public Map<Material, Double> loggingExp = new EnumMap<Material, Double>(Material.class);
	/** TODO PLEASE FIX */
	public double tradingExp = 0.0D;
	
	//Plugin reference.
	private AlfCore plugin;
	
	/**
	 * Load information for the given AlfCore plugin.
	 * @param plugin
	 */
	public void load(AlfCore plugin) {	
		this.plugin = plugin;
		FileConfiguration config = plugin.getConfig();
		config.options().copyDefaults(true);
		plugin.saveConfig();
		
		//Load all config sections.
		loadLevelConfig(config.getConfigurationSection("leveling"));
		loadClassConfig(config.getConfigurationSection("classes"));
		loadProperties(config.getConfigurationSection("properties"));
		loadManaConfig(config.getConfigurationSection("mana"));
		loadBedConfig(config.getConfigurationSection("bed"));
		loadHatsConfig(config.getConfigurationSection("hats"));
		loadBonusConfig(config.getConfigurationSection("bonus"));
	}
	
	/**
	 * Load level related config.
	 * @param section
	 */
	private void loadLevelConfig(ConfigurationSection section) {
		if (section != null) {
			this.power = Util.toDoubleNonNull(section.get("exp-curve", 1.0D), "exp-curve");
			maxExp = Util.toIntNonNull(section.get("max-exp", 100000), "max-exp");
			maxLevel = Util.toIntNonNull(section.get("max-level", 20), "max-level");
			padMaxLevel = section.getBoolean("pad-max-level", true);
			this.maxPartySize = Util.toIntNonNull(section.get("max-party-size"), "max-party-size");
			this.partyBonus = Util.toDoubleNonNull(section.get("party-exp-bonus", 0.2D), "party-exp-bonus");
			this.expLoss = Util.toDoubleNonNull(section.get("exp-loss", 0.05D), "expLoss");
			this.pvpExpLossMultiplier = Util.toDoubleNonNull(section.get("pvp-exp-loss", 1.0D), "pvp-exp-loss");
			this.levelsViaExpLoss = section.getBoolean("level-loss", false);
			this.masteryLoss = section.getBoolean("mastery-loss", false);
			this.noSpawnCamp = section.getBoolean("spawner-checks", false);
			this.spawnCampExpMult = Util.toDoubleNonNull(section.get("spawner-exp-mult", 0.5D), "spawner-exp-mult");
			this.resetOnDeath = section.getBoolean("reset-on-death", false);
			this.pvpLevelRange = Util.toIntNonNull(section.get("pvp-range", 50), "pvp-range");
			this.minPvpLevel = Util.toIntNonNull(section.get("min-pvp-level", 1), "min-pvp-level");
			this.pvpExpRange = Util.toIntNonNull(section.get("pvp-exp-range", 10), "pvp-exp-range");
			this.pvpMaxExpRange = Util.toIntNonNull(section.get("pvp-max-exp-range", 40), "pvp-max-exp-range");
			this.pvpMaxExpRange -= this.pvpExpRange;
			AlfCore.log(Level.INFO, "Minimum PvP level is set to: " + this.minPvpLevel);
			calcExp();
			if (section.getBoolean("dum-exp-false", false))
				dumpExpLevels();
			calcPartyMultipliers();
		}
	}
	
	/** See Mathematica Notebook for Visualization */
	protected void calcExp() {
		levels = new int[maxLevel + 1];
		//Exponential relation for experience gain.
		double A = maxExp * Math.pow(maxLevel-1, -(this.power + 1.0D));
		//Calculate experience quantities for each level.
		for (int i = 0; i < maxLevel + 1; i++)
			levels[i] = ((int)(A*Math.pow(i, this.power+1.0D)));
		levels[maxLevel-1] = maxExp;
	}
	
	/** Generate a file listing all of the exp required for the given levels. */
	public void dumpExpLevels() {
		File levelFile = new File(this.plugin.getDataFolder(), "levels.text");
		if (levelFile.exists())
			levelFile.delete();
		BufferedWriter bos = null;
		try {
			levelFile.createNewFile();
			bos = new BufferedWriter(new FileWriter(levelFile));
			for (int i = 0; i < maxLevel; i++)
				bos.append(i + " - " + getTotalExp(i+1)+"\n");
		} catch (FileNotFoundException e) {} catch (IOException e) {}
		finally {
			try { bos.close(); } catch (IOException e) {}
		}
	}

	/** Calculate party multipliers. */
	protected void calcPartyMultipliers() {
		partyMults = new double[this.maxPartySize];
		for (int i = 0; i < this.maxPartySize; i++)
			partyMults[i] = ((this.maxPartySize - 1.0D) / (this.maxPartySize * Math.log(this.maxPartySize)) * Math.log(i + 1));
	}

	/** Get the total exp for this level, including all levels beneath. */
	public static int getTotalExp(int level) {
		if (level >= levels.length)
			return levels[levels.length - 1];
		if (level < 1)
			return levels[0];
		return levels[level - 1];
	}

	/** Get the total exp for this level alone. */
	public static int getExp(int level) {
		if (level <= 1)
			return 0;
		return getTotalExp(level) - getTotalExp(level - 1);
	}

	/** Given a quantity of exp, determine what level corresponds. */
	public static int getLevel(double exp) {
		for (int i = maxLevel - 1; i >= 0; i--) {
			if (exp >= levels[i])
				return i+1;
		}
		return -1;
	}
	
	/**
	 * Load bonus config.
	 * @param section
	 */
	private void loadBonusConfig(ConfigurationSection section) {
		if (section != null) {
			this.expBonus = section.getDouble("exp", 1.0D);
			this.expiration = section.getLong("expiration", 0L);
			this.bonusMessage = section.getString("message");
		}
	}
	
	/**
	 * Load hats config.
	 * @param section
	 */
	private void loadHatsConfig(ConfigurationSection section) {
		if (section != null) {
			this.hatsLevel = Util.toIntNonNull(section.get("level", 1), "level");
			this.allowHats = section.getBoolean("enabled", false);
		}
	}
	
	/**
	 * Load bed config.
	 * @param section
	 */
	private void loadBedConfig(ConfigurationSection section) {
		if (section != null) {
			this.bedHeal = section.getBoolean("enabled", true);
			this.healInterval = Util.toIntNonNull(section.get("interval", 30), "interval");
			this.healPercent = Util.toIntNonNull(section.get("percent", 5), "percent");
		}
	}
	
	/** Load Mana Config. */
	private void loadManaConfig(ConfigurationSection section) {
		if (section != null) {
			this.manaRegenInterval = Util.toIntNonNull(section.get("interval",5), "interval");
			this.manaRegenPercent = Util.toIntNonNull(section.get("percent", 5), "percent");

			if ((this.manaRegenPercent > 100) || (this.manaRegenPercent < 0))
				this.manaRegenPercent = 5; //This is seemingly arbitrary.
		}
	}
	
	/** Load Properties. */
	private void loadProperties(ConfigurationSection section) {
		if (section != null) {
			this.storageType = section.getString("storage-type");
			this.economy = section.getBoolean("economy", false);
			this.debug = section.getBoolean("debug", false);
			this.foodHealPercent = Util.toDoubleNonNull(section.get("food-heal-percent", 0.05D), "food-heal-percent");
			this.globalCooldown = Util.toIntNonNull(section.get("global-cooldown", 1), "global-cooldown");
			this.blockTrackingDuration = Util.toIntNonNull(section.get("block-tracking-duration", 600000), "block-tracking-duration");
			this.maxTrackedBlocks = Util.toIntNonNull(section.get("max-tracked-blocks", 1000), "max-tracked-blocks");
			this.enchantXPMultiplier = Util.toDoubleNonNull(section.get("enchant-exp-mult", 1), "enchant-exp-mult");
			this.slowCasting = section.getBoolean("slow-while-casting", true);
			combatTime = Util.toIntNonNull(section.get("combat-time", 10000), "combat-time");
		}
	}

	/** Load Class Config. */
	private void loadClassConfig(ConfigurationSection section) {
		if (section != null) {
			this.prefixClassName = section.getBoolean("use-prefix", false);
			this.resetExpOnClassChange = section.getBoolean("reset-exp-on-change", true);
			this.resetMasteryOnClassChange = section.getBoolean("reset-master-on-change", false);
			this.resetProfMasteryOnClassChange = section.getBoolean("reset-prof-master-on-change", false);
			this.resetProfOnPrimaryChange = section.getBoolean("reset-prof-on-pri-change", false);
			this.lockPathTillMaster = section.getBoolean("lock-till-master", false);
			this.lockAtHighestTier = section.getBoolean("lock-at-max-level", false);
			this.swapMasterFree = section.getBoolean("master-swap-free", true);
			this.firstSwitchFree = section.getBoolean("first-swap-free", true);
			this.swapCost = Util.toDoubleNonNull(section.get("swap-cost", 0), "swap-cost");
			this.oldClassSwapCost = Util.toDoubleNonNull(section.get("old-swap-cost", 0), "old-swap-cost");
			this.profSwapCost = Util.toDoubleNonNull(section.get("prof-swap-cost", 0.0D), "prof-swap-cost");
			this.profSwapPower = Util.toDoubleNonNull(section.get("prof-swap-pow", 3.0D), "prof-swap-pow");
		}
	}
	
	/** Save config. */
	public void saveConfig() {
		FileConfiguration config = this.plugin.getConfig();
		config.set("bonus.message", this.bonusMessage);
		config.set("bonus.exp", this.expBonus);
		config.set("bonus.expiration", this.expiration);
		this.plugin.saveConfig();
	}
}
