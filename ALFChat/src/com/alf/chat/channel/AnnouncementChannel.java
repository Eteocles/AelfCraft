package com.alf.chat.channel;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.alf.AlfPlugin;
import com.alf.chat.ChPlayer;
import com.alf.util.Messaging;

/**
 * A channel which can only be listened to.
 * @author Eteocles
 */
public class AnnouncementChannel extends ChatChannel {

	//Message list for the annnouncement timer.
	private List<String> messages;
	//Alf plugin reference.
	private AlfPlugin plugin;
	//Message frequency.
	private int frequency;
	//Iterator for the message list.
	private Iterator<String> it;
	
	/**
	 * Constructs a Announcement Channel.
	 * @param name
	 * @param iden
	 * @param colorPrefix
	 * @param permission
	 */
	public AnnouncementChannel(String name, String iden, ChatColor colorPrefix,
			String permission, List<String> messages, int frequency, AlfPlugin plugin) {
		super(name, iden, colorPrefix, permission);
		this.messages = messages;
		this.plugin = plugin;
		this.it = messages.iterator();
		//Frequency in ticks.
		this.frequency = frequency;
		
		this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
			new AnnouncementTimer(this), frequency, frequency
		);
	}

	/**
	 * Announcement type.
	 */
	public ChannelType getType() {
		return ChatChannel.ChannelType.ANNOUNCE;
	}
	
	/**
	 * Get the frequency (actually period) of messages.
	 * @return
	 */
	public int getFrequency() {
		return this.frequency;
	}
	
	/**
	 * Set the frequency of messages.
	 * @param frequency
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * Get the next message for announcing.
	 * @return
	 */
	public String getNextMessage() {
		if (messages.isEmpty())
			return null;
		if (it.hasNext())
			return it.next();
		else {
			it = messages.iterator();
			return it.next();
		}
	}

	/**
	 * Suppresses player messaging in this channel.
	 */
	public void sendMessage(ChPlayer sender, String message, Object[] args) {
		Messaging.send(sender.getPlayer(), "[$1]$2 This channel does not permit talking.", 
				new Object[] {getIden(), ChatColor.RED}, getColorPrefix());
	}

	/**
	 * Sends a message to all member players.
	 */
	public void broadcast(String message) {
		for (ChPlayer player : getPlayers()) {
			Player p = player.getPlayer();
			Messaging.send(p, "[$1] "+ message, new Object[] {getIden()}, getColorPrefix());
		}
	}

	/**
	 * AnnouncementTimer runs repeatedly and displays a message. 
	 * @author Eteocles
	 */
	class AnnouncementTimer implements Runnable {
		private AnnouncementChannel ac;
	
		public AnnouncementTimer(AnnouncementChannel ac) {
			this.ac = ac;
		}
		
		public void run() {
			String message = ac.getNextMessage();
			if (message != null)
				ac.broadcast(message);
			//If there are no message to display, don't do anything.
		}
	}
	
}
