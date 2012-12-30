package com.alf.chat;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.AlfPlugin;
import com.alf.chat.commands.ChannelCommand;
import com.alf.chat.commands.ChannelListenCommand;
import com.alf.chat.commands.ChannelWhoCommand;
import com.alf.chat.commands.LeaveAllChannelCommand;
import com.alf.chat.commands.LeaveChannelCommand;
import com.alf.chat.commands.ListChannelsCommand;
import com.alf.chat.commands.ToggleSlowCommand;
import com.alf.chat.listener.ChPlayerListener;

/**
 * Chat plugin for Aelfcraft, handles chat functionality.
 * Chat Channels, Censors, Slow Chat Mode (Per User), Announcements.
 * @author Eteocles
 */
public class AlfChat extends AlfPlugin {

	public static AlfCore corePlugin;
	private ChatManager chatManager;
	private ConfigManager configManager;
	
	/**
	 * 
	 */
	public AlfChat() {
		super("AlfChat");
		this.commandParser = new CommandHandler();
	}

	/**
	 * Get the plugin's Chat manager.
	 * @return
	 */
	public ChatManager getChatManager() {
		return chatManager;
	}
	
	/**
	 * Set the Chat Manager.
	 * @param chatManager
	 */
	public void setChatManager(ChatManager chatManager) {
		this.chatManager = chatManager;
	}	
	
	/**
	 * Set up the AlfChat during enabling.
	 */
	public void setup() {
		if (! setupCore()) {
			log(Level.WARNING, "ALFChat requires ALFCore! Please install it before running.");
			getServer().getPluginManager().disablePlugin(this);
		}
		else {
			this.configManager = new ConfigManager(this);
			try {
				this.configManager.load();
			} catch (Exception e) {
				e.printStackTrace();
				log(Level.SEVERE, "Critical error encountered while loading. Disabling...");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			this.chatManager = new ChatManager(this);
			
			if (! this.configManager.loadManagers()) {
				getPluginLoader().disablePlugin(this);
				log(Level.SEVERE, "Critical error encountered while loading. Disabling...");
				getServer().getPluginManager().disablePlugin(this);
			}
		}
	}

	public void postSetup() {
		Player players[] = getServer().getOnlinePlayers();
		for (Player p : players) {
			this.chatManager.getChPlayer(p);
		}
	}

	protected void registerCommands() {
		commandParser.addCommand(new LeaveChannelCommand(this));
		commandParser.addCommand(new ChannelCommand(this));
		commandParser.addCommand(new ListChannelsCommand(this));
		commandParser.addCommand(new ChannelWhoCommand(this));
		commandParser.addCommand(new ChannelListenCommand(this));
		commandParser.addCommand(new LeaveAllChannelCommand(this));
		commandParser.addCommand(new ToggleSlowCommand(this));
	}

	protected void registerEvents() {
		Bukkit.getPluginManager().registerEvents(new ChPlayerListener(this), this);
	}

	/**
	 * Set up hook to main plugin.
	 * @return whether the hook exists
	 */
	private boolean setupCore() {
		return (corePlugin = (AlfCore) getServer().getPluginManager().getPlugin("AelfCore")) != null;
	}

}
