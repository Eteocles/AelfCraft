package com.alf.chat;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.alf.chat.persistence.YMLChPlayerStorage;
import com.alf.chat.util.Mail;

/**
 * Handles AlfChat configuration.
 * @author Eteocles
 */
public class ConfigManager {
	
	protected final AlfChat plugin;
	//Filters
	protected static File filterConfigFile;
	//Channels
	protected static File channelConfigFile;
	//Pending mail
	protected static File pendingMailConfigFile;
	
	private static Configuration filterConfig;
	private static Configuration channelConfig;
	private static Configuration pendingMailConfig;
	
	/**
	 * Constructs a Config Manager.
	 * @param plugin
	 */
	public ConfigManager(AlfChat plugin) {
		this.plugin = plugin;
		File dataFolder = plugin.getDataFolder();
		filterConfigFile = new File(dataFolder, "filters.yml");
		channelConfigFile = new File(dataFolder, "channels.yml");
		pendingMailConfigFile = new File(dataFolder, "pending-mail.yml");
	}
	
	public void load() throws Exception {
		checkForConfig(filterConfigFile);
		checkForConfig(channelConfigFile);
		checkForConfig(pendingMailConfigFile);
	}
	
	/**
	 * Load in all of the managers
	 * @return - successful or not
	 */
	public boolean loadManagers() {
		InputStream defConfigStream;
		
		filterConfig = YamlConfiguration.loadConfiguration(filterConfigFile);
		defConfigStream = this.plugin.getResource("defaults" + File.separator + "filters.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			filterConfig.setDefaults(defConfig);
		}
		
		channelConfig = YamlConfiguration.loadConfiguration(channelConfigFile);
		defConfigStream = this.plugin.getResource("defaults" + File.separator + "channels.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			channelConfig.setDefaults(defConfig);
		}
		
		pendingMailConfig = YamlConfiguration.loadConfiguration(pendingMailConfigFile);
		defConfigStream = this.plugin.getResource("defaults" + File.separator + "pending-mail.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			pendingMailConfig.setDefaults(defConfig);
		}
		
		this.plugin.getChatManager().loadChannels(channelConfig);
		this.plugin.getChatManager().loadFilters(filterConfig);
		this.plugin.getChatManager().loadMail(pendingMailConfig);
		return true;
	}
	
	/**
	 * Save mail configuration.
	 * @param pendingMail
	 */
	public void saveMailConfig(Map<String, Map<String, List<Mail>>> pendingMail) {
		for (String recipient : pendingMail.keySet()) {
			Map<String, List<Mail>> senders = pendingMail.get(recipient);
			ConfigurationSection section = pendingMailConfig.getConfigurationSection(recipient);
			if (section == null)
				pendingMailConfig.createSection(recipient);
			for (String sender : senders.keySet()) {
				List<String> messages = YMLChPlayerStorage.getMailSave(senders.get(sender));
				section.set(sender, messages);
			}
		}
	}
	
	/**
	 * Reload configuration.
	 * @return
	 */
	public boolean reload() {
		try {
			this.plugin.getChatManager().shutdown();
			filterConfig = null;
			channelConfig = null;
			this.plugin.setChatManager(null);
			loadManagers();
		} catch (Exception e) {
			e.printStackTrace();
			AlfChat.log(Level.SEVERE, "Critical error encountered while reloading. Disabling...");
			this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
			return false;
		}
		AlfChat.log(Level.INFO, "Reloaded confifugration.");
		return true;
	}
	
	/**
	 * Check for an existing config file or generate defaults.
	 * @param config
	 */
	public void checkForConfig(File config) {
		if (! config.exists()) {
			try {
				AlfChat.log(Level.WARNING, "File " + config.getName() + " not found - generating defaults.");
				config.getParentFile().mkdir();
				config.createNewFile();
				OutputStream output = new FileOutputStream(config, false);
				InputStream input = ConfigManager.class.getResourceAsStream("/defaults/" + config.getName());
				byte[] buf = new byte[8192]; //2^13
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
	}
}
