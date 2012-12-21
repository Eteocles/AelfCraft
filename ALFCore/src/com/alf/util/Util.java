package com.alf.util;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * Handles utility methods.
 * @author Eteocles
 */
public class Util {

	/**
	 * Drop an item at a given location, naturally or not.
	 * @param l
	 * @param i
	 * @param naturally
	 */
	public static void dropItem(Location l, ItemStack i, boolean naturally) {
		if (! (l == null || i == null || i.getTypeId() < 1 || i.getAmount() < 1)) {
			World w = l.getWorld();
			if (! w.isChunkLoaded(l.getChunk())) {
				w.loadChunk(l.getChunk());
			}

			if (naturally)
				l.getWorld().dropItemNaturally(l, i);
			else
				l.getWorld().dropItem(l, i);
		}
	}

	public static void dropItems(Location l, Iterable<ItemStack> items, boolean naturally) {
		if (items != null)
			for (ItemStack i : items)
				dropItem(l, i, naturally);
	}

	public static void dropItems(Location l, Map<?, ItemStack> items, boolean naturally) {
		if (items != null)
			dropItems(l, items.values(), naturally);
	}

}
