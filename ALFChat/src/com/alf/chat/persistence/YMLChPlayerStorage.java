package com.alf.chat.persistence;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;
import com.alf.chat.util.Mail;

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
		
		File playerFile = new File(pFolder, player.getName().toLowerCase()+".yml");
		
		if (playerFile.exists()) {
			Configuration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
			ChPlayer cplayer = new ChPlayer(player);
			
			loadChannels(cplayer, playerConfig);
			loadMail(cplayer, playerConfig);
			
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
		saveMail(player, playerConfig);
		
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
	 * Save a player's mail.
	 * @param player
	 * @param config
	 */
	private void saveMail(ChPlayer player, Configuration config) {
		ConfigurationSection section = config.getConfigurationSection("unread-mail");
		if (section == null)
			section = config.createSection("unread-mail");
		Map<String, List<Mail>> mail = player.getUnreadMail();
		for (String s : mail.keySet())
			section.set(s, getMailSave(mail.get(s)));
		
		section = config.getConfigurationSection("read-mail");
		if (section == null)
			section = config.createSection("read-mail");
		mail = player.getReadMail();
		for (String s : mail.keySet())
			section.set(s, getMailSave(mail.get(s)));
	}
	
	/**
	 * Convert the Mail list to a save-able String list.
	 * @param mail
	 * @return
	 */
	public static List<String> getMailSave(List<Mail> mail) {
		List<String> mailBits = new ArrayList<String>();
		for (Mail m : mail) {
			String s = "";
			if (m.attached != null)
				s = m.message + "|||" + m.attached.getTypeId() + "|||" + m.attached.getAmount();
			else
				s = m.message;
			mailBits.add(s);
		}
		return mailBits;
	}
	
	/**
	 * Load in mail from config.
	 * @param player
	 * @param config
	 */
	private void loadMail(ChPlayer player, Configuration config) {
		if (config.getConfigurationSection("unread-mail") == null)
			config.createSection("unread-mail");
		if (config.getConfigurationSection("read-mail") == null)
			config.createSection("read-mail");
		
		Map<String, List<Mail>> unreadMail = getMailBits(config.getConfigurationSection("unread-mail"));
		Map<String, List<Mail>> readMail = getMailBits(config.getConfigurationSection("read-mail"));
		player.loadMail(unreadMail, readMail);
	}
	
	/**
	 * Get the mail bits from a section.
	 * @param section
	 * @return
	 */
	public static Map<String, List<Mail>> getMailBits(ConfigurationSection section) {
		Map<String, List<Mail>> mail = new HashMap<String, List<Mail>>();
		try {
			//Get the set of keys (sender names).
			Set<String> keys = section.getKeys(false);
			for (String name : keys) {
				List<Mail> mailBits = new ArrayList<Mail>();
				List<String> unparsed = section.getStringList(name);
				for (String r : unparsed) {
					String[] parts = r.split("|||");
					Mail m;
					if (r.contains("|||") && parts.length > 1)
						m = new Mail(parts[0], new ItemStack(Material.getMaterial(Integer.parseInt(parts[1]))
							, Integer.parseInt(parts[2])) );
					else
						m = new Mail(parts[0], null);
					mailBits.add(m);
				}
				mail.put(name, mailBits);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return mail;
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
