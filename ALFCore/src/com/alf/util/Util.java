package com.alf.util;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * Handles utility methods.
 * @author Eteocles
 */
public class Util {

	public static final HashSet<Byte> transparentIds;
	public static final Set<Material> transparentBlocks;

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

	/**
	 * Convert an ambiguous object value to an Integer.
	 * @param val
	 * @return
	 */
	public static Integer toInt(Object val) {
		 if (val instanceof String)
			 try {
				 return Integer.valueOf((String)val);
			 } catch (NumberFormatException e) {
				 return null;
			 }
		 if (!(val instanceof Number))
			 return null;
		 if (val instanceof Integer)
			 return (Integer)val;
		 if (val instanceof Double)
			 return Integer.valueOf(((Double)val).intValue());
		 if (val instanceof Float)
			 return Integer.valueOf(((Float)val).intValue());
		 if (val instanceof Long)
			 return Integer.valueOf(((Long)val).intValue());
		 if (val instanceof BigDecimal)
			 return Integer.valueOf(((BigDecimal)val).intValue());
		 return null;
	}

	/**
	 * Convert an ambiguous object value to a Double.
	 * @param val
	 * @return
	 */
	public static Double toDouble(Object val) {
		 if (val instanceof String)
			 try {
				 return Double.valueOf((String)val);
			 } catch (NumberFormatException e) {
				 return null;
			 }
		 if (!(val instanceof Number))
			 return null;
		 if (val instanceof Integer)
			 return Double.valueOf(((Integer)val).doubleValue());
		 if (val instanceof Double)
			 return (Double)val;
		 if (val instanceof Float)
			return Double.valueOf(((Float)val).doubleValue());
		 if (val instanceof Long)
			 return Double.valueOf(((Long)val).doubleValue());
		 if (val instanceof BigDecimal)
			 return Double.valueOf(((BigDecimal)val).doubleValue());
		 return null;
	}
	
	/** Convert parameter to an Int. Handles non-numeral case. */
	 public static int toIntNonNull(Object val, String name)
	 {
		 Integer newVal = toInt(val);
		 if (newVal == null)
			 throw new IllegalArgumentException(name + " must be a numeral!");
		 return newVal;
	 }

	 /** Convert parameter to a Double. Handles non-numeral case. */
	 public static double toDoubleNonNull(Object val, String name) {
		 Double newVal = toDouble(val);
		 if (newVal == null)
			 throw new IllegalArgumentException(name + " must be a numeral!");
		 return newVal;
	 }
	 
	 static {
		 transparentBlocks = new HashSet<Material>(22);
		 transparentBlocks.add(Material.AIR);
		 transparentBlocks.add(Material.SNOW);
		 transparentBlocks.add(Material.REDSTONE_WIRE);
		 transparentBlocks.add(Material.TORCH);
		 transparentBlocks.add(Material.REDSTONE_TORCH_OFF);
		 transparentBlocks.add(Material.REDSTONE_TORCH_ON);
		 transparentBlocks.add(Material.RED_ROSE);
		 transparentBlocks.add(Material.YELLOW_FLOWER);
		 transparentBlocks.add(Material.SAPLING);
		 transparentBlocks.add(Material.LADDER);
		 transparentBlocks.add(Material.STONE_PLATE);
		 transparentBlocks.add(Material.WOOD_PLATE);
		 transparentBlocks.add(Material.CROPS);
		 transparentBlocks.add(Material.LEVER);
		 transparentBlocks.add(Material.WATER);
		 transparentBlocks.add(Material.STATIONARY_WATER);
		 transparentBlocks.add(Material.RAILS);
		 transparentBlocks.add(Material.POWERED_RAIL);
		 transparentBlocks.add(Material.DETECTOR_RAIL);
		 transparentBlocks.add(Material.DIODE_BLOCK_OFF);
		 transparentBlocks.add(Material.DIODE_BLOCK_ON);
		 
		 transparentIds = new HashSet<Byte>(22);
		 
		 transparentIds.add((byte)Material.AIR.getId());
		 transparentIds.add((byte)Material.SNOW.getId());
		 transparentIds.add((byte)Material.REDSTONE_WIRE.getId());
		 transparentIds.add((byte)Material.TORCH.getId());
		 transparentIds.add((byte)Material.REDSTONE_TORCH_OFF.getId());
		 transparentIds.add((byte)Material.REDSTONE_TORCH_ON.getId());
		 transparentIds.add((byte)Material.RED_ROSE.getId());
		 transparentIds.add((byte)Material.YELLOW_FLOWER.getId());
		 transparentIds.add((byte)Material.SAPLING.getId());
		 transparentIds.add((byte)Material.LADDER.getId());
		 transparentIds.add((byte)Material.STONE_PLATE.getId());
		 transparentIds.add((byte)Material.WOOD_PLATE.getId());
		 transparentIds.add((byte)Material.CROPS.getId());
		 transparentIds.add((byte)Material.LEVER.getId());
		 transparentIds.add((byte)Material.WATER.getId());
		 transparentIds.add((byte)Material.STATIONARY_WATER.getId());
		 transparentIds.add((byte)Material.RAILS.getId());
		 transparentIds.add((byte)Material.POWERED_RAIL.getId());
		 transparentIds.add((byte)Material.DETECTOR_RAIL.getId());
		 transparentIds.add((byte)Material.DIODE_BLOCK_OFF.getId());
		 transparentIds.add((byte)Material.DIODE_BLOCK_ON.getId());
	 }
}
