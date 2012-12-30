package com.alf.chat;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import com.alf.chat.channel.AnnouncementChannel;
import com.alf.chat.channel.ChatChannel;
import com.alf.chat.channel.NormalChannel;
import com.alf.chat.persistence.ChPlayerStorage;
import com.alf.chat.persistence.YMLChPlayerStorage;
import com.alf.util.Messaging;

/**
 * Handles all chat related functionality.
 * @author Eteocles
 */
public class ChatManager {
	//Plugin reference
	private AlfChat plugin;
	//Map of all Chat Channels.
	private Map<String, ChatChannel> chatChannels;
	//Map of all Chat Players.
	private Map<String, ChPlayer> players;
	//Storage.
	private ChPlayerStorage playerStorage;
	//Filtered words.
	private Set<String> bannedPhrases;
	//
	private List<String> censors;
	//Threshold for caps limit.
	private static double CAPS_THRESHOLD = 75;
	//Number of milliseconds between slow-chat messages.
	public static long SLOW_INTERVAL = 15000L;
	public static long SLOW_LENGTH = 300000L;
	
	private static String DEFAULT_CHANNEL;
	private static String GUEST_CHANNEL;
	
	/**
	 * Constructs the Chat Manager.
	 * @param plugin
	 */
	public ChatManager(AlfChat plugin) {
		this.plugin = plugin;
		this.chatChannels = new HashMap<String, ChatChannel>();
		this.players= new HashMap<String, ChPlayer>();
		this.playerStorage = new YMLChPlayerStorage(plugin);
		this.bannedPhrases = new HashSet<String>();
	}
	
	/**
	 * Get the ChPlayer object encapsulating the player.
	 * @param player
	 * @return
	 */
	public ChPlayer getChPlayer(Player player) {
		ChPlayer cplayer = this.players.get(player.getName().toLowerCase());
		if (cplayer != null)
			return cplayer;
		//Load up player.
		cplayer = this.playerStorage.loadPlayer(player);
		addChPlayer(cplayer);
		
		//Allow the player to see default-channel talk.
		addPlayerToChannelAudience(cplayer, DEFAULT_CHANNEL);
		addPlayerToChannelAudience(cplayer, GUEST_CHANNEL);
		
		if (cplayer.getMainChannel() == null && player.hasPermission("alfchat.channel.default")) {
			cplayer.setMainChannel(DEFAULT_CHANNEL);
			Messaging.send(player, "You have joined the Default Channel!", new Object[0]);
		} else if (cplayer.getMainChannel() == null){
			cplayer.setMainChannel(GUEST_CHANNEL);
			Messaging.send(player, "You have joined the Guest Channel!", new Object[0]);
			//Add the guest to broadcasting channels.
			for (String s : chatChannels.keySet()) {
				if (chatChannels.get(s).getType() == ChatChannel.ChannelType.ANNOUNCE)
					addPlayerToChannelAudience(cplayer, s);
			}
		}
		
		//Loaded player has set of channels. Sync.
		for (String s : cplayer.getChannels()) {
			addPlayerToChannelAudience(cplayer, s);
		}
		return cplayer;
	}
	
	/**
	 * Add a player to the manager.
	 * @param player
	 * @return
	 */
	public ChPlayer addPlayer(Player player) {
		ChPlayer newPlayer = new ChPlayer(player);
		players.put(player.getName().toLowerCase(), newPlayer);
		return newPlayer;
	}

	/**
	 * Add a chat player to the manager.
	 * @param player
	 * @return
	 */
	public ChPlayer addChPlayer(ChPlayer player) {
		players.put(player.getName().toLowerCase(), player);
		return player;
	}

	/**
	 * Remove a player from the manager.
	 * @param player
	 */
	public void removePlayer(Player player) {
		removeChPlayer(player.getName().toLowerCase());
	}

	/**
	 * Removes a ChatPlayer from the manager.
	 * Occurs when the player logs off. 
	 * @param player
	 */
	public void removeChPlayer(ChPlayer player) {
		if (player != null) {
			if (player.hasChannels()) {
				Set<String> pChannels = player.getChannels();
				for (String s : pChannels) {
					ChatChannel channel = chatChannels.get(s);
					channel.removePlayer(player);
				}
			}
		}
		players.remove(player.getName().toLowerCase());
	}

	/** Remove a Chat Player from the manager. */
	public void removeChPlayer(String name) {
		if (players.containsKey(name.toLowerCase()))
			removeChPlayer(players.get(name));
	}

	/** 
	 * Save a Chat Player... 
	 */
	public void saveChPlayer(Player player, boolean now)
	{	saveChPlayer(getChPlayer(player), now);	}
	
	/** 
	 * Save a ChatPlayer... 
	 */
	public void saveChPlayer(ChPlayer player, boolean now) {
		this.playerStorage.savePlayer(player, now);
		AlfChat.log(Level.INFO, "Saved ChPlayer: "+player.getPlayer().getName());
	}
	
	/**
	 * Get a chat channel by its name.
	 * @param ch
	 * @return
	 */
	public ChatChannel getChannel(String ch) {
		ChatChannel channel =  this.chatChannels.get(ch);
		if (channel == null)
			return getChannelFromIden(ch);
		else return channel;
	}
	
	/**
	 * Get a channel's full name.
	 * @param ch
	 * @return
	 */
	public String getFullChannelName(String ch) {
		ChatChannel channel = getChannel(ch);
		if (channel == null)
			return null;
		return channel.getName();
	}
	
	/**
	 * Add a new channel.
	 * @param ch
	 * @param iden
	 * @param col
	 * @return
	 */
	public ChatChannel addChannel(String ch, String iden, ChatColor col, ChatChannel.ChannelType type) {
		ChatChannel newChannel;
		switch (type) {
			case REGULAR:
				newChannel = new NormalChannel(ch, iden, col, "*");
				this.chatChannels.put(ch, newChannel);
				return newChannel;
			case PRIVILEGED:
				AlfChat.log(Level.WARNING, "Privileged channels are unsupported.");
				break;
			case ANNOUNCE:
				AlfChat.log(Level.WARNING, "Announcement channels can not be created this way.");
				break;
		}
		return null;
	}
	
	/**
	 * Close the given channel.
	 * @param ch
	 */
	public void closeChannel(String ch) {
		if (this.chatChannels.containsKey(ch)) {
			ChatChannel channel = chatChannels.remove(ch);
			//Announce to all channel members that the channel is closed.
			channel.broadcast("Channel " + ch + " has closed.");
			//Remove the channel from all members.
			for (ChPlayer p : channel.getPlayers())
				p.leaveChannelAudience(channel);
			//Once method leaves the scope, channel will be erased from memory.
			//No need to remove players from the channel.
		}
	}
	
	/**
	 * Add the given player to the channel audience.
	 * @param p
	 * @param ch
	 * @return
	 */
	public boolean addPlayerToChannelAudience(Player p, String ch) {
		return addPlayerToChannelAudience(getChPlayer(p), getChannel(ch));
	}
	
	public boolean addPlayerToChannelAudience(ChPlayer player, String ch) {
		return addPlayerToChannelAudience(player, getChannel(ch));
	}
	
	/**
	 * Add the channel player to the chat channel.
	 * @param player
	 * @param channel
	 * @return
	 */
	public boolean addPlayerToChannelAudience(ChPlayer player, ChatChannel channel) {
		if (channel != null) {
			if (! channel.playerHasPermission(player.getPlayer())) {
				Messaging.send(player.getPlayer(), "You do not have permissions to listen to channel $1", 
						new Object[] {ChatColor.BOLD + channel.getName()}, ChatColor.RED);
				return false;
			}
			channel.addPlayer(player);
			player.joinChannelAudience(channel.getName());
			return true;
		}
		return false;
	}
	
	/**
	 * Remove a player from the channel audience.
	 * @param p
	 * @param ch
	 * @return
	 */
	public boolean removePlayerFromChannelAudience(Player p, String ch) {
		ChatChannel channel = getChannel(ch);
		ChPlayer player = getChPlayer(p);
		
		if (channel != null) {
			channel.removePlayer(player);
			player.leaveChannelAudience(channel.getName());
			return true;
		}
		
		return false;
	}
	
	/**
	 * Get channel from identifier.
	 * @param iden
	 * @return
	 */
	public ChatChannel getChannelFromIden(String iden) {
		for (ChatChannel ch : this.chatChannels.values())
			if (ch.getIden().equalsIgnoreCase(iden))
				return ch;
		return null;
	}
	
	/**
	 * Get the channels stored in this map.
	 * @return
	 */
	public Map<String, ChatChannel> getChannels() {
		return Collections.unmodifiableMap(this.chatChannels);
	}
	
	/**
	 * Get the players stored in this map.
	 * @return
	 */
	public Map<String, ChPlayer> getPlayers() {
		return Collections.unmodifiableMap(this.players);
	}
	
	/**
	 * Get a String representation of all of the channels.
	 * @param player
	 * @return
	 */
	public String getChannelList(Player player) {
		String availableChannels = "Channels: ";
		//List available channels.
		for (ChatChannel ch : this.chatChannels.values()) {
			if (ch.getType() == ChatChannel.ChannelType.ANNOUNCE)
				availableChannels += ChatColor.AQUA;
			else if (ch.getPermission().equals("*") || player.hasPermission(ch.getPermission()))
				availableChannels += ChatColor.GREEN;
			else
				availableChannels += ChatColor.RED;
			availableChannels += ch.getName() + " (" + ch.getIden() + "), ";
		}
		availableChannels = availableChannels.substring(0, availableChannels.length() - 2);
		return availableChannels;
	}
	
	public void loadFilters(Configuration config) {
		this.bannedPhrases = new HashSet<String>(config.getStringList("banned-phrases"));
		this.censors = config.getStringList("censor-replacements");
	}
	
	/**
	 * Load channels from configuration.
	 * @param config
	 */
	public void loadChannels(Configuration config) {
		List<String> channels = config.getStringList("channels");
		if (channels != null) {
			for (String s : channels) {
				ChatColor color = ChatColor.GOLD;
				String permission = "*";
				String identifier = s;
				int type  = 1; //1 - Normal, 2 - Privileged, 3 - Announcement
				List<String> messages = new ArrayList<String>();;
				int frequency = 1000;
				//Get color definition.
				if (config.getString(s + ".color") != null) {
					color = ChatColor.valueOf(config.getString(s + ".color"));
					if (color == null)
						color = ChatColor.GOLD;
				}
				//Get permission definition.
				if (config.getString(s + ".permission") != null) {
					permission = config.getString(s + ".permission");
				}
				//Get identifier definition.
				if (config.getString(s + ".identifier") != null) {
					identifier = config.getString(s + ".identifier");
				}
				//Get type definition.
				if (config.getInt(s + ".type") > 0) {
					type = config.getInt(s + ".type");
				}
				//
				if (type == 3) {
					if (config.getStringList(s + ".announcements") != null) 
						messages = config.getStringList(s + ".announcements");
					if (config.getInt(s + ".frequency") > 0)
						frequency = config.getInt(s + ".frequency");
				}
				switch (type) {
					case 1:
						chatChannels.put(s, new NormalChannel(s, identifier, color, permission));
						break;
					case 2:
						break;
					case 3:
						chatChannels.put(s, new AnnouncementChannel(s, identifier, 
								color, permission, messages, frequency, plugin));
						break;
				}
			}
		} else AlfChat.log(Level.WARNING, "No channels section defined!");
		
		DEFAULT_CHANNEL = config.getString("default");
		GUEST_CHANNEL = config.getString("guest");
		
		if (DEFAULT_CHANNEL == null) {
			AlfChat.log(Level.WARNING, "No default channel defined!");
			DEFAULT_CHANNEL = this.chatChannels.values().iterator().next().getName();
		}
		else {
			AlfChat.log(Level.INFO, "Loaded the default channel: " + DEFAULT_CHANNEL);
		}
		
		if (GUEST_CHANNEL == null) {
			AlfChat.log(Level.WARNING, "No guest channel defined!");
			GUEST_CHANNEL = DEFAULT_CHANNEL;
		}
		else {
			AlfChat.log(Level.INFO, "Loaded the guest channel: " + GUEST_CHANNEL);
		}
	}
	
	/**
	 * Passes a filter through a message.
	 * Weights by percentage of capitalization and amount of profanity.
	 * Will force slow chat on a player for a period of time if the weight is too large.
	 * @param message
	 */
	public String filter(String message, ChPlayer player) {
		//Check for capitalization ratio.
		int numCaps = 0;
		double capsPercentage = 0;
		for (int i = 0; i < message.length(); i++) {
			if (Character.isUpperCase(message.charAt(i))) 
				numCaps++;
		}
		capsPercentage = numCaps / message.length();
		if (capsPercentage >= CAPS_THRESHOLD)
			message = message.toLowerCase();
		
		boolean censored = false;
		
		//Check for profanity.
		String trimmedMessage = message.replaceAll("\\W", "").toLowerCase();
		for (String s : bannedPhrases) {
			if (trimmedMessage.contains(s)) {
				message = this.censors.get((int)(Math.random() * censors.size()));		
				censored = true;
				break;
			}
		}
		
		if (capsPercentage >= CAPS_THRESHOLD || censored) {
			if (player.getPlayer().hasPermission("alfchat.filter.bypass") || player.getPlayer().isOp())
				return message;
			player.setSlow(true);
			//TODO Get the Alf representation from AlfCore and lower karma.
		}
		
		return message;
	}
	
	/**
	 * Shut down the manager.
	 */
	public void shutdown() {
		this.playerStorage.shutdown();
	}
}
