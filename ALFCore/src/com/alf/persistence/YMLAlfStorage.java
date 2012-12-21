package com.alf.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;
import com.alf.chararacter.classes.AlfClass;

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
//			AlfClass playerClass = loadClass(player, playerConfig);
//			if (playerClass == null) {
//				AlfCore.log(Level.INFO, "Invalid class found for " + player.getName() + ". Resetting player.");
//				return createNewAlf(player);
//			}
//			AlfClass secondClass = loadSecondaryClass(player, playerConfig);
			//TODO
			Alf playerAlf = new Alf(this.plugin, player, null, null);
			
			playerAlf.setMana(playerConfig.getInt("mana", 0));
			playerAlf.setHealth(playerConfig.getInt("health", 100)); //TODO CHANGE FROM 100
			playerAlf.setVerbose(playerConfig.getBoolean("verbose", true));
//			playerAlf.setSuppresedSkills(playerConfig.getStringList("suppressed"));
			
			AlfCore.log(Level.INFO, "Loaded alf: " + player.getName() + " with EID: " + player.getEntityId());
			return playerAlf;
		}
		AlfCore.log(Level.INFO, "Created alf: " + player.getName());
		return createNewAlf(player);
	}
	
	/**
	 * 
	 * @param player
	 * @param config
	 * @return
	 */
	public AlfClass loadClass(Player player, Configuration config) {
		AlfClass playerClass = null;
		AlfClass defaultClass = this.plugin.getClassManager().getDefaultClass();
		
		if (config.getString("class") != null) {
			playerClass = this.plugin.getClassManager().getClass(config.getString("class"));
			if (playerClass == null)
				playerClass = defaultClass;
			else if (! playerClass.isPrimary())
				playerClass = defaultClass;
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
//		playerConfig.set("suppressed", new ArrayList<String>(alf.getSuppressedSkills()));
		playerConfig.set("mana", alf.getMana());
		playerConfig.set("health", alf.getHealth());
		
		saveSkillSettings(alf, playerConfig.createSection("skill-settings"));
//		saveCooldowns(alf, playerConfig.createSection("cooldowns"));
		saveExperience(alf, playerConfig.createSection("experience"));
		saveBinds(alf, playerConfig.createSection("binds"));
		
		playerConfig.save(playerFile);
		
		return true;
	}
	
	private void saveSkillSettings(Alf alf, ConfigurationSection config) {
		
	}
	
	private void saveExperience(Alf alf, ConfigurationSection config) {
		
	}
	
	private void saveBinds(Alf alf, ConfigurationSection config) {
		
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
