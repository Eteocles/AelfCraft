package com.alf.chat;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.alf.chat.channel.ChatChannel;
import com.alf.chat.util.Mail;
import com.alf.util.Messaging;

/**
 * Encapsulates a Player object. 
 * @author Eteocles
 * Holds flags for chat plugin.
 */
public class ChPlayer {
	//Talking channel.
	private String mainChannel;
	//All channels.
	private Set<String> channels;
	/* Slow chat toggle */
	private boolean slowChat; 
	private long slowChatStart = -1;
	//Name
	private String name;
	//Player object.
	private Player player;
	//Last time of player chat.
	private long lastChatTime = -1;
	//Maps player name to mail.
	private Map<String, List<Mail>> unreadMail = new HashMap<String, List<Mail>>();
	private Map<String, List<Mail>> readMail = new HashMap<String, List<Mail>>();
	
	/**
	 * Constructs the ChPlayer.
	 * @param player
	 */
	public ChPlayer(Player player) {
		this.name = player.getName().toLowerCase();
		this.player = player;
		channels = new HashSet<String>();
		slowChat = false;
	}
	
	/**
	 * Whether the player is in any channels.
	 * @return
	 */
	public boolean hasChannels() {
		return ! channels.isEmpty();
	}
	
	/**
	 * Start listening to the given channel.
	 * @param ch
	 */
	public void joinChannelAudience(ChatChannel ch) {
		channels.add(ch.getName());
	}
	
	public void joinChannelAudience(String ch) {
		channels.add(ch);
	}
	
	/**
	 * Get the player's talking channel.
	 * @return
	 */
	public String getMainChannel() {
		return mainChannel;
	}
	
	/**
	 * Set the player's talking channel.
	 * @param newChannel
	 */
	public void setMainChannel(String newChannel) {
		this.mainChannel = newChannel;
	}
	
	/**
	 * Stop listening to the given channel.
	 * @param ch
	 */
	public void leaveChannelAudience(String ch) {
		channels.remove(ch);
	}
	
	public void leaveChannelAudience(ChatChannel ch) {
		channels.remove(ch.getName());
	}
	
	/**
	 * Get the player name;
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the player encapsulated by this object.
	 * @return
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * Whether the player is in slow-chat mode.
	 * @return
	 */
	public boolean isSlow() {
		return this.slowChat;
	}
	
	/**
	 * Set whether the player is in slow-chat mode.
	 * @param slowMode
	 */
	public void setSlow(boolean slowMode) {
		if (slowMode) {
			this.slowChatStart = System.currentTimeMillis();
			Messaging.send(player.getPlayer(), "Watch your tongue! Slow chat mode enabled for " + 
					ChatManager.SLOW_LENGTH / 1000L + " seconds!", new Object[0], ChatColor.RED);
			AlfChat.log(Level.INFO, "Slow Chat Started for Player " + player.getName() + " at time " + this.slowChatStart);
		} else {
			this.slowChatStart = -1;
			Messaging.send(this.player, "Slow chat mode disabled.", new Object[0]);
		}
		this.slowChat = slowMode;
	}
	
	/**
	 * Get the channels this player is listening in.
	 * @return
	 */
	public Set<String> getChannels() {
		return Collections.unmodifiableSet(channels);
	}
	
	/**
	 * Add a collection of channels to this player.
	 * @param channels
	 */
	public void addChannels(Collection<String> channels) {
		this.channels.addAll(channels);
	}
	
	/**
	 * Set the channels for this player.
	 * @param channels
	 */
	public void setChannels(Set<String> channels) {
		this.channels = channels;
	}
	
	/**
	 * Get the last chat time.
	 * @return
	 */
	public long getLastChatTime() {
		return this.lastChatTime;
	}
	
	/**
	 * Get the time at which slow chat started.
	 * @return slow chat time or -1 if not in slow chat
	 */
	public long getSlowChatStart() {
		return this.slowChatStart;
	}
	
	/**
	 * Update the chat time.
	 */
	public void updateChatTime() {
		this.lastChatTime = System.currentTimeMillis();
		if (slowChat && this.lastChatTime > ChatManager.SLOW_LENGTH + this.slowChatStart) {
			this.slowChat = false;
			Messaging.send(this.player, "Slow chat mode disabled.", new Object[0]);
			slowChatStart = -1;
		}
	}
	
	/**
	 * Send player mail!
	 * @param player
	 * @param message
	 */
	public void sendMail(Player player, Mail mail) {
		List<Mail> pendingMail = this.unreadMail.get(player.getName());
		if (pendingMail == null)
			pendingMail = new ArrayList<Mail>();
		pendingMail.add(mail);
		this.unreadMail.put(player.getName(), pendingMail);
		notifyPlayerMail();
	}
	
	/**
	 * Notify a player of mail.
	 */
	public void notifyPlayerMail() {
		if (! this.unreadMail.isEmpty()) {
			int size = 0;
			for (String s : unreadMail.keySet())
				size += unreadMail.get(s).size();
			Messaging.send(getPlayer(), "You have $1 unread messages!", new Object[] { size }, ChatColor.GOLD);
		}
	}
	
	/**
	 * Read player mail.
	 * @param name
	 * @return
	 */
	public List<Mail> readUnreadMail(String name) {
		List<Mail> pendingMail = this.unreadMail.remove(name);
		this.readMail.put(name, pendingMail);
		
		return pendingMail;
	}
	
	/**
	 * Load in mail from config call.
	 * @param unread
	 * @param read
	 */
	public void loadMail(Map<String, List<Mail>> unread, Map<String, List<Mail>> read) {
		this.unreadMail.putAll(unread);
		this.readMail.putAll(read);
	}
	
	/**
	 * Save the unread mail.
	 * Called from the Player Storage.
	 * @return
	 */
	public Map<String, List<Mail>> getUnreadMail() {
		return this.unreadMail;
	}
	
	/**
	 * Saave the read mail.
	 * Called from the Player Storage.
	 * @return
	 */
	public Map<String, List<Mail>> getReadMail() {
		return this.readMail;
	}
	
	/**
	 * Whether two ChPlayers are equal.
	 * @param cplayer
	 * @return
	 */
	public boolean equals(ChPlayer cplayer) {
		if (cplayer == null) return false;
		return (cplayer.getName().equalsIgnoreCase(name));
	}
	
}
