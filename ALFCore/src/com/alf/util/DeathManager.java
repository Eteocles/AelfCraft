package com.alf.util;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.alf.AlfCore;

/**
 * Manages players who die.
 * @author Eteocles
 */
public class DeathManager {
	
	private AlfCore plugin;
	//Map player names to their stored inventories.
	private Map<String, PlayerInventoryStorage> deathInventories = new HashMap<String, PlayerInventoryStorage>();
	private Map<String, Integer> taskIds = new HashMap<String, Integer>();
	//Timeout length before a queued Alf's inventory is removed.
	private static final long STORAGE_TIMEOUT = 3000L;
	
	/**
	 * Constructs the Death Manager.
	 * @param alfCore
	 */
	public DeathManager(AlfCore alfCore) {
		this.plugin = alfCore;
	}
	
	/**
	 * Store the dead player and the linked event.
	 * @param event
	 */
	public void queuePlayer(EntityDeathEvent event) {
		final Player p = (Player) event.getEntity();
		deathInventories.put(p.getName(), new PlayerInventoryStorage(event));
		taskIds.put(p.getName(), this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, 
			new Runnable() { public void run() {
				DeathManager.this.deathInventories.remove(p.getName());
				DeathManager.this.taskIds.remove(p.getName());
			}}, STORAGE_TIMEOUT));
	}
	
	/**
	 * Remove and get the Player Inventory Storage object linked to the once dead player.
	 * @param player
	 * @return
	 */
	public PlayerInventoryStorage popPlayer(Player player) {
		plugin.getServer().getScheduler().cancelTask(taskIds.remove(player.getName()));
		return deathInventories.remove(player.getName());
	}
	
	/**
	 * Whether the death manager has a player stored.vx
	 * Use as a test to see if a player is dead and retained in the manager.
	 * @param player
	 * @return
	 */
	public boolean containsPlayer(Player player) {
		return deathInventories.containsKey(player.getName());
	}
	
	/**
	 * Stores a player's inventory.
	 * @author Eteocles
	 */
	public class PlayerInventoryStorage {
		
		private String playerName;
		private List<StoredItemStack> inventory;
		
		public PlayerInventoryStorage(EntityDeathEvent event) {
			Player p = (Player)event.getEntity();
			playerName = p.getName();
			PlayerInventory playerInv = p.getInventory();
			int invSize = playerInv.getSize() + playerInv.getArmorContents().length;
			inventory = new ArrayList<StoredItemStack>(invSize);
			
			for (int slot = 0; slot < invSize; slot++) {
				ItemStack item = playerInv.getItem(slot);
				if (item != null) {
					ItemStack keptItem = item.clone();
					inventory.add(new StoredItemStack(slot, keptItem));
				}
			}
		}
		
		public String getPlayerName() {
			return playerName;
		}
		
		public List<StoredItemStack> getInventory() {
			return inventory;
		}
		
	}
	
	/**
	 * Describes an item stack and its corresponding location in a Player's inventory.
	 * @author Eteocles
	 */
	public class StoredItemStack {
		
		private int slot;
		private ItemStack item;
		
		public StoredItemStack(int slot, ItemStack item) {
			this.slot = slot;
			this.item = item;
		}
		
		public int getSlot() {
			return slot;
		}
		
		public ItemStack getItem() {
			return item;
		}
		
	}
	
}
