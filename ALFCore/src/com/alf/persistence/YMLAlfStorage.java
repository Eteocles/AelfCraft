package com.alf.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.Pet;
import com.alf.character.classes.AlfClass;
import com.alf.util.Properties;
import com.alf.util.Util;

/**
 * Stores Alf data in YML.
 * @author Eteocles
 */
public class YMLAlfStorage extends AlfStorage {

	private final File playerFolder;
	//Queue for Alfs to save.
	private Map<String, Alf> toSave = new ConcurrentHashMap<String, Alf>();
	private final long SAVE_INTERVAL = 6000;
	private int id = 0;

	/**
	 * Constructs the YML storage.
	 * @param plugin
	 */
	public YMLAlfStorage(AlfCore plugin) {
		super(plugin);
		this.playerFolder = new File(plugin.getDataFolder(), "players");
		this.playerFolder.mkdirs();

		this.id = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
				new AlfSaveThread(), SAVE_INTERVAL, SAVE_INTERVAL).getTaskId();
	}

	/**
	 * Load an Alf from a given player.
	 */
	public Alf loadAlf(Player player) {
		if (this.toSave.containsKey(player.getName())) {
			Alf alf = (Alf) this.toSave.get(player.getName());
			alf.setPlayer(player);
			return alf;
		}
		File pFolder = new File(this.playerFolder, player.getName().toLowerCase().substring(0, 1));
		pFolder.mkdirs();
		File playerFile = new File(pFolder, player.getName() + ".yml");

		if (playerFile.exists()) {
			Configuration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
			
			AlfClass playerClass = loadClass(player, playerConfig);
			
			if (playerClass == null) {
				AlfCore.log(Level.INFO, "Invalid class found for " + player.getName() + ". Resetting player.");
				return createNewAlf(player);
			}
			AlfClass secondClass = loadSecondaryClass(player, playerConfig);

			Alf playerAlf = new Alf(this.plugin, player, playerClass, secondClass);

			loadCooldowns(playerAlf, playerConfig.getConfigurationSection("cooldowns"));
			loadExperience(playerAlf, playerConfig.getConfigurationSection("experience"));
			loadBinds(playerAlf, playerConfig.getConfigurationSection("binds"));
			loadSkillSettings(playerAlf, playerConfig.getConfigurationSection("skill-settings"));
			playerAlf.setMana(playerConfig.getInt("mana", 0));
			playerAlf.setHealth(playerConfig.getInt("health", playerClass.getBaseMaxHealth()));
			playerAlf.setVerbose(playerConfig.getBoolean("verbose", true));
			playerAlf.setSuppressedSkills(playerConfig.getStringList("suppressed"));
			
			Location playerLoc = player.getLocation();
			Location safeLoc = null;
			for (int i = -2; i <= 2; i++) {
				for (int j = -2; j <= 2; j++) {
					Location tempLoc = new Location(player.getWorld(), i+playerLoc.getBlockX(), 
							playerLoc.getBlockY(), j + playerLoc.getBlockZ());
					if (tempLoc.getBlock().isEmpty()) {
						safeLoc = tempLoc;
						break;
					}
				}
			}
			
			if (safeLoc == null)
				safeLoc = playerLoc;
			
			EntityType petType = EntityType.fromName(playerConfig.getString("pet", null));
			if (petType != null) {
				Pet pet = Util.spawnPet(plugin, playerAlf, petType, safeLoc);
				AlfCore.log(Level.INFO, "Loaded pet: " + pet.getName() + " with EID : " +pet.getEntity().getUniqueId());
			}

			AlfCore.log(Level.INFO, "Loaded alf: " + player.getName() + " with EID: " + player.getEntityId());
			return playerAlf;
		}
		AlfCore.log(Level.INFO, "Created alf: " + player.getName());
		return createNewAlf(player);
	}

	/**
	 * Load skill settings.
	 * @param alf
	 * @param section
	 */
	private void loadSkillSettings(Alf alf, ConfigurationSection section) {
		if ((section == null) || (section.getKeys(false) == null))
			return;

		String skill;
		ConfigurationSection skillSection;

		//Iterate through all of the skills listed and put settings.
		for (Iterator<String> it = section.getKeys(false).iterator(); it.hasNext(); ) { 
			skill = (String)it.next();
			if (section.isConfigurationSection(skill)) {
				skillSection = section.getConfigurationSection(skill);
				for (String key : skillSection.getKeys(true))
					if (!skillSection.isConfigurationSection(key))
						alf.setSkillSetting(skill, key, skillSection.get(key)); 
			} 
		} 
	}

	/**
	 * Load in binds.
	 * @param alf
	 * @param section
	 */
	private void loadBinds(Alf alf, ConfigurationSection section) {
		if (section == null)
			return;
		Set<String> bindKeys = section.getKeys(false);
		if ((bindKeys != null) && (bindKeys.size() > 0))
			for (String material : bindKeys)
				try {
					Material item = Material.valueOf(material);
					String bind = section.getString(material, "");
					if (bind.length() > 0)
						alf.bind(item, bind.split(" "));
				}
				catch (IllegalArgumentException e) {
					AlfCore.log(Level.WARNING, new StringBuilder().append(material).append(" isn't a valid Item to bind a Skill to.").toString());
				}
	}

	/**
	 * Load player experience.
	 * @param playerAlf
	 * @param section
	 */
	private void loadExperience(Alf playerAlf,
			ConfigurationSection section) {
		if (playerAlf == null || playerAlf.getAlfClass() == null || section == null)
			return;
		Set<String> expList = section.getKeys(false);
		
		if (expList != null)
			for (String className : expList) {
				double exp = section.getDouble(className, 0.0D);
				AlfClass alfClass = this.plugin.getClassManager().getClass(className);
				if (alfClass != null && playerAlf.getExperience(alfClass) == 0) {
					if (exp > Properties.maxExp)
						exp = Properties.maxExp;
					playerAlf.setExperience(alfClass, exp);
				}
			}
	}

	/**
	 * Load cool downs.
	 * @param alf
	 * @param section
	 */
	private void loadCooldowns(Alf alf, ConfigurationSection section) {
		if (section == null)
			return;
		Set<String> storedCooldowns = section.getKeys(false);
		long time;
		if (storedCooldowns != null) {
			time = System.currentTimeMillis();
			for (String skillName : storedCooldowns)
				try {
					long cooldown = Long.valueOf(section.getString(skillName, "0"));

					if (alf.hasAccessToSkill(skillName) && cooldown > time)
						alf.setCooldown(skillName, cooldown);
				}
				catch (NumberFormatException e) {}
		}
	}

	/**
	 * 
	 * @param player
	 * @param config
	 * @return
	 */
	public AlfClass loadClass(Player player, Configuration config) {
//		AlfCore.log(Level.INFO, "Checking for a class...");
		AlfClass playerClass = null;
		AlfClass defaultClass = this.plugin.getClassManager().getDefaultClass();

		if (config.getString("class") != null) {
//			AlfCore.log(Level.INFO, "Found class: " + config.getString("class"));
			
			playerClass = this.plugin.getClassManager().getClass(config.getString("class"));
			if (playerClass == null)
				playerClass = defaultClass;
			else if (! playerClass.isPrimary())
				playerClass = defaultClass;
			
//			AlfCore.log(Level.INFO, "Got class: " + playerClass.toString());
		}
		else
			playerClass = defaultClass;
		return playerClass;
	}

	/**
	 * 
	 * @param player
	 * @param config
	 * @return
	 */
	public AlfClass loadSecondaryClass(Player player, Configuration config) {
		AlfClass playerClass = null;

		if (config.getString("secondary-class") != null) {
			playerClass = this.plugin.getClassManager().getClass(config.getString("secondary-class"));
			if (playerClass == null || ! playerClass.isSecondary()) {
				AlfCore.log(Level.SEVERE, "Invalid secondary class was defined for " + player.getName() + " resetting to nothing!");
				return null;
			}
		}
		return playerClass;
	}

	/**
	 * Save the given Alf, either later or instantly.
	 */
	public void saveAlf(Alf alf, boolean now) {
		if (now)
			try {
				doSave(alf);
			} catch (IOException e) {
				AlfCore.log(Level.SEVERE, "There was a problem saving the Alf: " + alf.getName());
				this.toSave.put(alf.getName(), alf);
			}
		else {
			this.toSave.put(alf.getName(), alf);
		}
	}

	/**
	 * Save all Alf values.
	 * @param alf
	 * @throws IOException
	 */
	public boolean doSave(Alf alf) throws IOException {
		String name = alf.getName();
		File playerFile = new File(this.playerFolder+File.separator+name.substring(0, 1).toLowerCase(), 
				name + ".yml");
		FileConfiguration playerConfig = new YamlConfiguration();

		//Save Alf class.
		playerConfig.set("class", alf.getAlfClass().toString());
		//Save second class.
		if (alf.getSecondClass() != null) {
			playerConfig.set("secondary-class", alf.getSecondClass().toString());
		}
		//
		playerConfig.set("verbose", alf.isVerbose());
		playerConfig.set("suppressed", new ArrayList<String>(alf.getSuppressedSkills()));
		playerConfig.set("mana", alf.getMana());
		playerConfig.set("health", alf.getHealth());
		playerConfig.set("pet", alf.getPet() == null ? null : alf.getPet().getEntity().getType().getName());
		
		saveSkillSettings(alf, playerConfig.createSection("skill-settings"));
		saveCooldowns(alf, playerConfig.createSection("cooldowns"));
		saveExperience(alf, playerConfig.createSection("experience"));
		saveBinds(alf, playerConfig.createSection("binds"));

		playerConfig.save(playerFile);

		return true;
	}

	/**
	 * Save skill settings.
	 * @param alf
	 * @param config
	 */
	private void saveSkillSettings(Alf alf, ConfigurationSection config) {
		Map.Entry<String, ConfigurationSection> entry;
		for (Iterator<Entry<String, ConfigurationSection>> it = alf.getSkillSettings().entrySet().iterator(); it.hasNext(); ) { 
			entry = (Map.Entry<String,ConfigurationSection>)it.next();
			for (String key : entry.getValue().getKeys(true))
				if (!entry.getValue().isConfigurationSection(key))
					config.set(entry.getKey()+"."+key, entry.getValue().get(key));
		}
	}

	/**
	 * Save the experience.
	 * @param alf
	 * @param config
	 */
	private void saveExperience(Alf alf, ConfigurationSection config) {
		if (alf == null || alf.getClass() == null || config == null)
			return;
		
		Map<String, Double> expMap = alf.getExperienceMap();
		for (Map.Entry<String, Double> entry : expMap.entrySet())
			config.set((String) entry.getKey(), entry.getValue());
	}
	
	/**
	 * Save cooldowns.
	 * @param alf
	 * @param section
	 */
	private void saveCooldowns(Alf alf, ConfigurationSection section)
	{
		if (section == null) {
			AlfCore.debugLog(Level.SEVERE, new StringBuilder().append("Could not create cooldown section for ").append(alf.getName()).toString());
			return;
		}

		long time = System.currentTimeMillis();
		Map<String, Long> cooldowns = alf.getCooldowns();
		for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
			String skillName = (String)entry.getKey();
			Long cooldown = (Long)entry.getValue();
			if (cooldown.longValue() > time) {
				AlfCore.debugLog(Level.INFO, alf.getName()+": - "+skillName+" @ "+cooldown);
				section.set(skillName, cooldown.toString());
			}
		}
	}

	/**
	 * Save binds.
	 * @param alf
	 * @param section
	 */
	private void saveBinds(Alf alf, ConfigurationSection section) {
		if (section == null)
			return;
		Map<Material, String[]> binds = alf.getBinds();
		for (Material material : binds.keySet()) {
			String[] bindArgs = (String[]) binds.get(material);
			String bind = "";
			for (String arg : bindArgs)
				bind += arg + " ";
			section.set(material.toString(), bind.substring(0, bind.length() - 1));
		}
	}


	/**
	 * Shut down 
	 */
	public void shutdown() {
		Bukkit.getScheduler().cancelTask(this.id);
		Collection<Alf> unsaved = this.plugin.getCharacterManager().getAlfs();
		Iterator<Entry<String, Alf>> iter = this.toSave.entrySet().iterator();
		while (iter.hasNext()) {
			Alf alf = (Alf) ((Map.Entry<String, Alf>)iter.next()).getValue();
			try {
				doSave(alf);
				unsaved.remove(alf);
			} catch (Exception e) {
				AlfCore.log(Level.SEVERE, "There was a problem saving the alf: " + alf.getName());
			}
			iter.remove();
		}
		for (Alf alf : unsaved)
			try {
				doSave(alf);
			} catch(Exception e) {
				AlfCore.log(Level.SEVERE, "There was a problem saving the alf: " + alf.getName());
			}
	}

	/**
	 * Handles timed, repeated saving of Alfs.
	 * @author Eteocles
	 */
	protected class AlfSaveThread implements Runnable {

		protected AlfSaveThread() {}

		/**
		 * Repeated function of save thread. 
		 */
		public void run() {
			if (! YMLAlfStorage.this.toSave.isEmpty()) {
				Iterator<Entry<String, Alf>> iter = YMLAlfStorage.this.toSave.entrySet().iterator();
				//Iterate through all of the Alfs to be saved.
				while (iter.hasNext()) {
					Alf alf = (Alf) ((Map.Entry<String, Alf>)iter.next()).getValue();
					try {
						YMLAlfStorage.this.doSave(alf);
					} catch (Exception e) {
						AlfCore.log(Level.SEVERE, "There was a problem saving the Alf: " + alf.getName());
						e.printStackTrace();
					}
					//Remove the current iterative element before advancing.
					iter.remove();
				}
			}
		}
	}

}
