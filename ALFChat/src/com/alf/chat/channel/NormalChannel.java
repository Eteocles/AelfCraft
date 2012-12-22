package com.alf.chat.channel;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.alf.chat.ChPlayer;
import com.alf.util.Messaging;

/**
 * Normal type chat channel in which all players can talk/listen.
 * @author Eteocles
 */
public class NormalChannel extends ChatChannel {

	public NormalChannel(String name, String iden, ChatColor colorPrefix,
			String permission) {
		super(name, iden, colorPrefix, permission);
	}

	/**
	 * Get this channel's type.
	 */
	public ChannelType getType() {
		return ChatChannel.ChannelType.REGULAR;
	}
	
	/**
	 * Sends the message to all players in the channel.
	 * @param sender - player sending the message
	 * @param message - message to be broadcasted
	 * @param args - regular expressions
	 */
	public void sendMessage(ChPlayer sender, String message, Object[] args) {
		for (ChPlayer player : getPlayers()) {
			Player p = player.getPlayer();
			Messaging.send(p, "[$1]$2 $3: " + message, 
					new Object[] {getIden(), ChatColor.WHITE, sender.getPlayer().getDisplayName()}, getColorPrefix());
		}
	}
	
	/**
	 * Sends a message to all players in the channel. Used for announcements or by moderators/admins.
	 * @param message - message to be broadcasted
	 * @param args - regular expressions
	 */
	public void broadcast(String message) {
		for (ChPlayer player : getPlayers()) {
			Player p = player.getPlayer();
			Messaging.send(p, "[$1] Broadcast: "+ message, new Object[] {getIden()}, ChatColor.GREEN);
		}
	}
	
}
