package com.alf.chat.persistence;

import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;

/**
 * Stores and handles players.
 * @author Eteocles
 */
public abstract class ChPlayerStorage {

protected AlfChat plugin;
	
	public ChPlayerStorage(AlfChat plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Create a new player for the storage from a regular one.
	 * @param player
	 * @return
	 */
	public ChPlayer createNewPlayer(Player player) {
		ChPlayer cplayer = new ChPlayer(player);
		return cplayer;
	}
	
	public abstract ChPlayer loadPlayer(Player player);
	
	public abstract void savePlayer(ChPlayer player, boolean now);
	
	public abstract void shutdown();
	
}
