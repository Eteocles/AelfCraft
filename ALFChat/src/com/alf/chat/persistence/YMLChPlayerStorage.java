package com.alf.chat.persistence;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;

/**
 * Stores ChPlayer information in YML flatfile.
 * @author Eteocles
 */
public class YMLChPlayerStorage extends ChPlayerStorage {

	private final File playerFolder;
	private Map<String, ChPlayer> toSave = new ConcurrentHashMap<String, ChPlayer>();
	private final int SAVE_INTERVAL = 6000;
	private int id = 0;
	
	/**
	 * Constructs the YMLChPlayerStorage.
	 * @param plugin
	 */
	public YMLChPlayerStorage(AlfChat plugin) {
		super(plugin);
		this.playerFolder = new File(plugin.getDataFolder(), "players");
		this.playerFolder.mkdirs();
		this.id = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
				new ChPlayerSaveThread(), SAVE_INTERVAL, SAVE_INTERVAL
		).getTaskId();
	}

	/**
	 * Load ChPlayer information for a given player.
	 */
	public ChPlayer loadPlayer(Player player) {
		if (this.toSave.containsKey(player.getName())) {
			ChPlayer cplayer = this.toSave.get(player.getName().toLowerCase());
			return cplayer;
		}
		File pFolder = new File(this.playerFolder, player.getName().toLowerCase().substring(0, 1));
		pFolder.mkdirs();
		
		File playerFile = new File(pFolder, new StringBuilder().append(player.getName().toLowerCase()).append(".yml").toString());
		
		if (playerFile.exists()) {
			Configuration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
			ChPlayer cplayer = new ChPlayer(player);
			loadChannels(cplayer, playerConfig);
			return cplayer;
		}
		//Otherwise, if not found, create new player.
		AlfChat.log(Level.INFO, "Created ChPlayer: " + player.getName());
		return createNewPlayer(player);
	}

	/**
	 * Save a ChPlayer's information, either at a delay or instantly.
	 */
	public void savePlayer(ChPlayer player, boolean now) {
		try {
			doSave(player);
		} catch (IOException e) {
			AlfChat.log(Level.SEVERE, new StringBuilder().append("There was a problem saving the ChPlayer: ").append(player.getName()).toString());
			this.toSave.put(player.getName().toLowerCase(), player);
		}
	}
	
	/**
	 * Try saving player information flat file.
	 * @param player
	 * @return
	 * @throws IOException
	 */
	public boolean doSave(ChPlayer player) throws IOException {
		String name = player.getName();
		File playerFile = new File(new StringBuilder().append(this.playerFolder).append(File.separator).append(name.substring(0,1).toLowerCase()).toString(), name.toLowerCase() + ".yml");
		FileConfiguration playerConfig = new YamlConfiguration();
		
		playerConfig.set("audience channels", new ArrayList<String>(player.getChannels()));
		playerConfig.set("talk channel", player.getMainChannel());
		playerConfig.save(playerFile);
		
		return true;
	}
	
	/**
	 * Load a player's channels.
	 * @param player
	 * @param config
	 */
	private void loadChannels(ChPlayer player, Configuration config) {
		List<String> channels = config.getStringList("audience channels");
		player.setChannels(new HashSet<String>(channels));
		player.setMainChannel(config.getString("talk channel"));
	}

	/**
	 * Shut down the Player storage.
	 */
	public void shutdown() {
		Bukkit.getScheduler().cancelTask(this.id);
		Collection<ChPlayer> unsaved = this.plugin.getChatManager().getPlayers().values();
		for (ChPlayer player : toSave.values()) {
			try {
				doSave(player);
				//Removes if present.
				unsaved.remove(player);
			} catch (Exception e) {
				AlfChat.log(Level.SEVERE, "There was a problem saving the Chat Player: " + player.getName());
			}
		}
		
		for (ChPlayer player : unsaved) {
			try {
				doSave(player);
			} catch (Exception e) {
				AlfChat.log(Level.SEVERE, "There was a problem saving the Chat Player: " + player.getName());
			}
		}
	}
	
	/**
	 * Saves all queued players.
	 * @author Eteocles
	 */
	protected class ChPlayerSaveThread implements Runnable {
		
		protected ChPlayerSaveThread() {}
		
		public void run() {
			if (! YMLChPlayerStorage.this.toSave.isEmpty()) {
				Iterator<Entry<String, ChPlayer>> iter = YMLChPlayerStorage.this.toSave.entrySet().iterator();
				while (iter.hasNext()) {
					ChPlayer player = (ChPlayer)((Map.Entry<String, ChPlayer>)iter.next()).getValue();
					try {
						YMLChPlayerStorage.this.doSave(player);
					} catch (Exception e) {
						AlfChat.log(Level.SEVERE, "There was a problem saving the ChPlayer: " + player.getName());
						e.printStackTrace();
					}
					iter.remove();
				}
			}
		}
		
	}

}
