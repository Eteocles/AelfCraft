package com.alf.listener;

import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.util.Messaging;

/**
 * Listens to inventory events.
 * @author Eteocles
 */
public class AInventoryListener implements Listener {

	private AlfCore plugin;
	
	public AInventoryListener(AlfCore plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Handle craft preparation.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPrepareCraft(PrepareItemCraftEvent event) {
		Player player = (Player) event.getView().getPlayer();
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		
		if (! alf.canCraft(event.getRecipe().getResult())) {
			AlfCore.debugLog(Level.INFO, alf.getName() + " attempted to craft: " + event.getRecipe().getResult().getTypeId() + ": " + event.getRecipe().getResult().getDurability());
			event.getView().close();
			Messaging.send(alf.getPlayer(), "You don't know how to craft $1",  new Object[] { event.getRecipe().getResult().getType().name().toLowerCase().replace("_", " ") });
		}
	}
	
	/**
	 * Handle item crafting.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCraftItem(CraftItemEvent event) {
		if (! (event.getWhoClicked() instanceof Player ))
			return;
		
		Player player = (Player) event.getWhoClicked();
		if (event.isShiftClick() && player.getInventory().firstEmpty() == -1)
			return;
		
		ItemStack cursor = event.getCursor();
		ItemStack result = event.getRecipe().getResult();
		
		int amountCrafted = result.getAmount();
		if (!event.isShiftClick() && cursor != null && cursor.getType() != Material.AIR && 
				(cursor.getType() != result.getType() || cursor.getType().getMaxStackSize() < cursor.getAmount() + amountCrafted))
			return;
		
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		
		if (! alf.canCraft(result)) {
			Messaging.send(alf.getPlayer(), "You don't know how to craft $1!", new Object[] { result.getType().name().toLowerCase().replace("_", " ") });
			AlfCore.debugLog(Level.INFO, alf.getName() + " attempted to craft: " + result.getTypeId() + " : " + result.getDurability());
			event.setCancelled(true);
			return;
		}
	}
	
}
