package com.alf.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;

/**
 * Stores Alf data in YML.
 * @author Eteocles
 */
public class YMLAlfStorage extends AlfStorage {

	private final File playerFolder;
	//Queue for Alfs to save.
	private Map<String, Alf> toSave = new ConcurrentHashMap<String, Alf>();
	private final int SAVE_INTERVAL = 6000;
	private int id = 0;

	/**
	 * Constructs the YML storage.
	 * @param plugin
	 */
	public YMLAlfStorage(AlfCore plugin) {
		super(plugin);
		this.playerFolder = new File(plugin.getDataFolder(), "players");
		this.playerFolder.mkdirs();

		this.id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, 
				new AlfSaveThread(), SAVE_INTERVAL, SAVE_INTERVAL);
	}

	public Alf loadAlf(Player player) {
		// TODO Auto-generated method stub
		return null;
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
	

	public void shutdown() {
		// TODO Auto-generated method stub

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
