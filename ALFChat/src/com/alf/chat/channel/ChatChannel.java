package com.alf.chat.channel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.chat.ChPlayer;

public abstract class ChatChannel {

	//Channel specific name. Immutable.
	private final String name;
	//Shorthand labels.
	private final String iden;
	//Holds list of player references.
	private Set<ChPlayer> players;
	//ChatColor for Labeling
	private ChatColor colorPrefix;
	//
	private String permission;
	
	public static enum ChannelType {
		REGULAR,  //Channels which players can talk and listen in.
		PRIVILEGED, //Channels which only privileged players can talk in. 
		ANNOUNCE //Channels which can not be talked in, display timed messages.
	}
	
	/**
	 * Constructs the Chat Channel.
	 * @param name
	 * @param iden
	 * @param colorPrefix
	 * @param permission
	 */
	public ChatChannel(String name, String iden, ChatColor colorPrefix, String permission) {
		this.name = name;
		this.iden = iden;
		this.colorPrefix = colorPrefix;
		this.permission = permission;
		players = new HashSet<ChPlayer>();
	}
	
	/**
	 * Get channel name..
	 * @return - name of this chat channel
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get channel identifier.
	 * @return
	 */
	public String getIden() {
		return this.iden;
	}
	
	/**
	 * Permission node.
	 * @return
	 */
	public String getPermission() {
		return this.permission;
	}
	
	/**
	 * color prefix.
	 * @return
	 */
	public ChatColor getColorPrefix() {
		return this.colorPrefix;
	}
	
	/**
	 * Get the specific type of channel.
	 * @return
	 */
	public abstract ChannelType getType();
	
	/**
	 * Get list of all players.
	 * @return - all players in this chat channel
	 */
	public Set<ChPlayer> getPlayers() {
		return Collections.unmodifiableSet(players);
	}
	
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	/**
	 * Determine whether a player is in this channel.
	 * @param player - player to be searched for
	 * @return - if player is in the channel or not
	 */
	public boolean hasPlayer(ChPlayer player) {
		return players.contains(player);
	}
	
	public boolean playerHasPermission(Player player) {
		if (permission.equals("*"))
			return true;
		if (AlfCore.perms == null)
			return player.isOp();
		return player.hasPermission(permission);
	}
	
	/**
	 * Add a player to the channel.
	 * @param player - player joining the channel
	 * @return - whether the player is already in the channel or not
	 */
	public boolean addPlayer(ChPlayer player) {
		return players.add(player);
	}
	
	/**
	 * Remove a player from the channel.
	 * @param player - player leaving the channel
	 * @return - whether the player was in the channel or not
	 */
	public boolean removePlayer(ChPlayer player) {
		return players.remove(player);
	}
	
	/**
	 * Sends the message to all players in the channel.
	 * @param sender - player sending the message
	 * @param message - message to be broadcasted
	 * @param args - regular expressions
	 */
	public abstract void sendMessage(ChPlayer sender, String message, Object[] args);
	
	/**
	 * Sends a message to all players in the channel. Used for announcements or by moderators/admins.
	 * @param message - message to be broadcasted
	 * @param args - regular expressions
	 */
	public abstract void broadcast(String message);
	
	/**
	 * Reset channel.
	 */
	public void reset() {
		this.players.clear();
		broadcast("The chat channel "+ name + " has been purged.");
	}
	
}
