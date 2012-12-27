package com.alf.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

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
	
	public double potHealthPerTier;

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
	
	/** Save config. */
	public void saveConfig() {
		FileConfiguration config = this.plugin.getConfig();
		config.set("bonus.message", this.bonusMessage);
		config.set("bonus.exp", this.expBonus);
		config.set("bonus.expiration", this.expiration);
		this.plugin.saveConfig();
	}
}
