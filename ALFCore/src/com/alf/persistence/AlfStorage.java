package com.alf.persistence;

import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;

/**
 * Handles storage of Alf player information.
 * @author Eteocles
 */
public abstract class AlfStorage {
	
	protected AlfCore plugin;
	
	/**
	 * Constructs an AlfStorage.
	 * @param plugin
	 */
	public AlfStorage(AlfCore plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Create a new encapsulating Alf for a player.
	 * @param player - player whose information to use
	 * @return - the new alf
	 */
	public Alf createNewAlf(Player player) {
		Alf alf = new Alf(this.plugin, player, null, null);
		alf.setMana(100);
		alf.setHealth(100);
//		alf.syncHealth();
		return alf;
	}
	
	/**
	 * Load an Alf object from storage using the Player.
	 * @param player
	 * @return
	 */
	public abstract Alf loadAlf(Player player);
	
	/**
	 * Save the Alf either on a delay or at the present moment.
	 * @param alf
	 * @param now
	 */
	public abstract void saveAlf(Alf alf, boolean now);
	
	/**
	 * Shut down the AlfStorage.
	 * Save any Alfs that were queued for delayed saving.
	 */
	public abstract void shutdown();
}
