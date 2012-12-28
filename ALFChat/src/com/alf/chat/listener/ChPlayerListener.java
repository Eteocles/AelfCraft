package com.alf.chat.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;
import com.alf.chat.ChatManager;
import com.alf.util.Messaging;

/**
 * Listens to Player related events.
 * @author Eteocles
 */
public class ChPlayerListener implements Listener {

	public final AlfChat plugin;

	/**
	 * Constructs a ChPlayerListener.
	 * @param plugin
	 */
	public ChPlayerListener(AlfChat plugin) {
		this.plugin = plugin;
	}

	/**
	 * Reroutes chat messages through channels.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		//Get the channels the sender is in, if any.
		ChPlayer player = this.plugin.getChatManager().getChPlayer(event.getPlayer());
		ChatManager cm = this.plugin.getChatManager();
		
		if (player.isSlow()) {
			if (player.getLastChatTime() + ChatManager.SLOW_INTERVAL > System.currentTimeMillis()) {
				//If not enough time has elapsed between slow chats, cancel.
				Messaging.send(player.getPlayer(), "Not enough time has elapsed since your last message. Please wait $1 seconds.", 
						new Object[] { (int)((player.getLastChatTime() + ChatManager.SLOW_INTERVAL - System.currentTimeMillis()) / 1000L) },
						ChatColor.RED);
				event.setCancelled(true);
				return;
			}
		}
		
		String mainChannel = player.getMainChannel();
		if (mainChannel != null) {
			//Check message for filters and profanity.
			cm.getChannel(player.getMainChannel()).sendMessage(player, cm.filter(event.getMessage(), player), new Object[0]);
			player.updateChatTime();
		} else {
			Messaging.send(player.getPlayer(), "You are not in any channels!", new Object[0], ChatColor.GRAY);
		}
		event.setCancelled(true);
	}

	/**
	 * Handles player joining.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		ChatManager cm = this.plugin.getChatManager();
		ChPlayer player = cm.getChPlayer(event.getPlayer());
		//List channels to player.
		String channelList =  ChatColor.AQUA+"Chat Channels: ";
		for (String channel : player.getChannels()) {
			if (player.getMainChannel().equals(channel))
				channelList += ChatColor.DARK_AQUA + channel + " " + ChatColor.AQUA;
			else channelList += channel + " ";
		}
		event.getPlayer().sendMessage(channelList);
	}

	/**
	 * Handles player leaving.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent event) {
		ChatManager cm = this.plugin.getChatManager();
		Player player = event.getPlayer();
		cm.saveChPlayer(player, true);
		cm.removePlayer(player);
	}
}
