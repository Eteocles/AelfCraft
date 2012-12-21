package com.alf.listener;

import net.minecraft.server.EntityPlayer;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.entity.CraftPlayer;
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
		
		for (Command command : this.plugin.getCommandHandler().getCommands())
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
