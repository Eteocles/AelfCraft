package com.alf.chat;

import java.io.*;
import java.util.logging.Level;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

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
	
	private static Configuration filterConfig;
	private static Configuration channelConfig;
	
	/**
	 * Constructs a Config Manager.
	 * @param plugin
	 */
	public ConfigManager(AlfChat plugin) {
		this.plugin = plugin;
		File dataFolder = plugin.getDataFolder();
		filterConfigFile = new File(dataFolder, "filters.yml");
		channelConfigFile = new File(dataFolder, "channels.yml");
	}
	
	public void load() throws Exception {
		checkForConfig(filterConfigFile);
		checkForConfig(channelConfigFile);
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
		
		this.plugin.getChatManager().loadChannels(channelConfig);
		
		return true;
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
