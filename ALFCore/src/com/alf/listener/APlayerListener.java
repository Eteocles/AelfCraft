package com.alf.listener;

import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;

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
	 * Handle player respawning.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		final Alf alf = this.plugin.getCharacterManager().getAlf(player);
//		alf.setHealth(alf.getMaxHealth());
		alf.setMana(0);
		
		PlayerInventory pi = player.getInventory();
		for (ItemStack is : alf.getInvFromDeath())
			pi.addItem(is);
		
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
