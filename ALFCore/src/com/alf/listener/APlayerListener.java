package com.alf.listener;


import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import net.minecraft.server.v1_4_5.EntityPlayer;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;
import com.alf.chararacter.CharacterManager;
import com.alf.chararacter.effect.CombatEffect;
import com.alf.command.Command;
import com.alf.util.DeathManager;
import com.alf.util.DeathManager.PlayerInventoryStorage;
import com.alf.util.DeathManager.StoredItemStack;
import com.alf.util.Util;

/**
 * Handle player related events.
 * @author Eteocles
 */
public class APlayerListener implements Listener {

	private final AlfCore plugin;
	
	/**
	 * Constructs an APlayerListener.
	 * @param plugin
	 */
	public APlayerListener(AlfCore plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Handle player joining.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		CharacterManager cm = this.plugin.getCharacterManager();
		final Alf alf = cm.getAlf(player);
		
		/**
		 * Forcefully reset combat effect.
		 */
		if (! alf.hasEffect("Combat")) {
			alf.resetCombatEffect();
		}
		
//		cm.checkClass(alf);
//		alf.syncExperience();
//		alf.syncHealth();
//		alf.checkInventory();
		
		//class prefix name
		
		//bonus expiration
		
		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
//				APlayerListener.this.plugin.getCharacterManager().performSkillChecks(alf);
//				alf.checkInventory();
			}
		}, 5L);
		player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Welcome to AelfCraft!");
	}
	
	/**
	 * Handle player quitting.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		CharacterManager alfManager = this.plugin.getCharacterManager();
		Alf alf = alfManager.getAlf(player);
		alf.cancelDelayedSkill();
		alf.clearEffects();
		if (alf.isInCombat())
			alf.leaveCombat(CombatEffect.LeaveCombatReason.LOGOUT);
		
		alfManager.saveAlf(alf, true);
		alfManager.removeAlf(alf);
		
		for (Command command : this.plugin.getCommandParser().getCommands())
			if (command.isInteractive())
				command.cancelInteraction(player);
	}
	
	/**
	 * Handle player respawning.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		final Alf alf = this.plugin.getCharacterManager().getAlf(player);
//		alf.setHealth(alf.getMaxHealth());
		alf.setMana(0);
		
		//If the DeathManager contains the stored player...
		DeathManager dm = this.plugin.getDeathManager();
		if (dm.containsPlayer(player)) {
			AlfCore.log(Level.INFO, "Death Manager contains player: " + player.getName());
			//Remove player from DeathManager.
			PlayerInventoryStorage invStore = dm.popPlayer(player);
			PlayerInventory playerInv = player.getInventory();
			List<StoredItemStack> inv = invStore.getInventory();
			//Restore items or drop them if needed.
			for (StoredItemStack is : inv) {
				//If spot is occupied...
				if (playerInv.getItem(is.getSlot()) != null) {
					HashMap<Integer, ItemStack> leftovers = playerInv.addItem(new ItemStack[] { is.getItem() });
					if (leftovers.size() > 0)
		            	Util.dropItems(player.getLocation(), leftovers, false);
				} 		           
				else 
	            	playerInv.setItem(is.getSlot(), is.getItem());
			}
		}
		
		CraftPlayer craftPlayer = (CraftPlayer) player;
		EntityPlayer entityPlayer = craftPlayer.getHandle();
		entityPlayer.exp = 0.0F;
		entityPlayer.expTotal = 0;
		entityPlayer.expLevel = 0;
//		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
//			public void run() {
//				APlayerListener.this.plugin.getCharacterManager().performSkillChecks(alf);
//				alf.checkInventory();
//				alf.syncExperience();
//			}
//		}, 20L);
	}
	
}
