package com.alf.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;

/**
 * Handles utility methods.
 * @author Eteocles
 */
public class Util {

	public static final HashSet<Byte> transparentIds;
	public static final Set<Material> transparentBlocks;
	public static final List<String> swords;
	public static final List<String> axes;
	public static final List<String> shovels;
	public static final List<String> picks;
	public static final List<String> hoes;
	public static final List<String> weapons;
	public static final List<String> armors;
	public static final List<String> tools;
	
	public static final HashMap<String, Location> deaths = new LinkedHashMap<String, Location>() {
		private static final long serialVersionUID = -5160276589164566330L;
		private static final int MAX_ENTRIES = 50;
		protected boolean removeEldestEntry(Map.Entry<String, Location> eldest) {
			return size() > MAX_ENTRIES;
		}
	};

	/**
	 * Move an item in an Alf's inventory from a slot.
	 * @param alf
	 * @param slot
	 * @param item
	 * @return
	 */
	public static boolean moveItem(Alf alf, int slot, ItemStack item) {
		Player player = alf.getPlayer();
		PlayerInventory inv = player.getInventory();
		int empty = firstEmpty(inv.getContents());
		//Full Inventory...
		if (empty == -1) {
			player.getWorld().dropItemNaturally(player.getLocation(), item);
			if (slot != -1)
				inv.clear(slot);
			return false;
		}
		//Add to inventory in empty spot.
		inv.setItem(empty, item);
		return true;
	}


	/**
	 * Get the first spot outside of the inventory's toolbar which is not empty, or -1 if full.
	 * @param contents
	 * @return
	 */
	public static int firstEmpty(ItemStack[] inventory) {
		for (int i = 9; i < inventory.length; i++)
			if (inventory[i] == null)
				return i;
		return -1;
	}


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
	 * Get the total Minecraft experience points for a given level.
	 * @param level
	 * @return
	 */
	public static int getMCExperience(int level) {
		//TODO Fix
		return level*7;
	}

	/**
	 * Sync the plugin's contained player with the server.
	 * @param player
	 * @param plugin
	 */
	public static void syncInventory(final Player player, AlfCore plugin) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				player.updateInventory();
			}
		});
	}

	/**
	 * Whether the parameter material is a weapon type.
	 * @param item
	 * @return
	 */
	public static boolean isWeapon(Material item) {
		switch (item) {
		case WOOD_SWORD:
		case WOOD_AXE:
		case WOOD_PICKAXE:
		case WOOD_SPADE:
		case GOLD_SWORD:
		case GOLD_AXE:
		case GOLD_PICKAXE:
		case GOLD_SPADE:
		case STONE_SWORD:
		case STONE_AXE:
		case STONE_PICKAXE:
		case STONE_SPADE:
		case IRON_SWORD:
		case IRON_AXE:
		case IRON_PICKAXE:
		case IRON_SPADE:
		case DIAMOND_SWORD:
		case DIAMOND_AXE:
		case DIAMOND_PICKAXE:
		case DIAMOND_SPADE:
		case WOOD_HOE:
		case GOLD_HOE:
		case STONE_HOE:
		case IRON_HOE:
		case DIAMOND_HOE:
		case BOW:
		case SNOW_BALL:
		case FISHING_ROD:
		default:
			return false;
		}
	}

	/**
	 * Whether the parameter material is an armor type.
	 * @param item
	 * @return
	 */
	public static boolean isArmor(Material item) {
		switch (item) {
		case LEATHER_BOOTS:
		case LEATHER_LEGGINGS:
		case LEATHER_CHESTPLATE:
		case LEATHER_HELMET:
		case GOLD_BOOTS:
		case GOLD_LEGGINGS:
		case GOLD_CHESTPLATE:
		case GOLD_HELMET:
		case IRON_BOOTS:
		case IRON_LEGGINGS:
		case IRON_CHESTPLATE:
		case IRON_HELMET:
		case DIAMOND_BOOTS:
		case DIAMOND_LEGGINGS:
		case DIAMOND_CHESTPLATE:
		case DIAMOND_HELMET:
		case CHAINMAIL_BOOTS:
		case CHAINMAIL_LEGGINGS:
		case CHAINMAIL_CHESTPLATE:
		case CHAINMAIL_HELMET:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Whether the parameter material is a food.
	 * @param item
	 * @return
	 */
	public static boolean isFood(Material item) {
		switch (item) {
		case APPLE:
		case MUSHROOM_SOUP:
		case BREAD:
		case PORK:
		case GRILLED_PORK:
		case GOLDEN_APPLE:
		case RAW_FISH:
		case COOKED_FISH:
		case CAKE:
		case COOKIE:
		case MELON:
		case RAW_BEEF:
		case COOKED_BEEF:
		case RAW_CHICKEN:
		case COOKED_CHICKEN:
		case ROTTEN_FLESH:
		case SPIDER_EYE:
		case CARROT:
		case POTATO:
		case BAKED_POTATO:
		case POISONOUS_POTATO:
			//		case GOLDEN_CARROT:
		case PUMPKIN_PIE:
			return true;
		default:
			return false;
		}
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
		swords = new ArrayList<String>(5);
		swords.add("WOOD_SWORD");
		swords.add("STONE_SWORD");
		swords.add("IRON_SWORD");
		swords.add("GOLD_SWORD");
		swords.add("DIAMOND_SWORD");

		axes = new ArrayList<String>(5);
		axes.add("WOOD_AXE");
		axes.add("STONE_AXE");
		axes.add("IRON_AXE");
		axes.add("GOLD_AXE");
		axes.add("DIAMOND_AXE");

		shovels = new ArrayList<String>(5);
		shovels.add("WOOD_SPADE");
		shovels.add("STONE_SPADE");
		shovels.add("IRON_SPADE");
		shovels.add("GOLD_SPADE");
		shovels.add("DIAMOND_SPADE");

		picks = new ArrayList<String>(5);
		picks.add("WOOD_PICKAXE");
		picks.add("STONE_PICKAXE");
		picks.add("IRON_PICKAXE");
		picks.add("GOLD_PICKAXE");
		picks.add("DIAMOND_PICKAXE");

		hoes = new ArrayList<String>(5);
		hoes.add("WOOD_HOE");
		hoes.add("STONE_HOE");
		hoes.add("IRON_HOE");
		hoes.add("GOLD_HOE");
		hoes.add("DIAMOND_HOE");

		tools = new ArrayList<String>(2);
		tools.add("SHEARS");
		tools.add("FISHING_ROD");
		tools.addAll(shovels);
		tools.addAll(hoes);

		weapons = new ArrayList<String>(26);
		weapons.addAll(picks);
		weapons.addAll(axes);
		weapons.addAll(swords);
		weapons.addAll(tools);
		weapons.add("BOW");

		armors = new ArrayList<String>(21);
		armors.add("LEATHER_HELMET");
		armors.add("LEATHER_LEGGINGS");
		armors.add("LEATHER_BOOTS");
		armors.add("LEATHER_CHESTPLATE");
		armors.add("IRON_HELMET");
		armors.add("IRON_LEGGINGS");
		armors.add("IRON_CHESTPLATE");
		armors.add("IRON_BOOTS");
		armors.add("CHAINMAIL_HELMET");
		armors.add("CHAINMAIL_LEGGINGS");
		armors.add("CHAINMAIL_BOOTS");
		armors.add("CHAINMAIL_CHESTPLATE");
		armors.add("GOLD_HELMET");
		armors.add("GOLD_LEGGINGS");
		armors.add("GOLD_CHESTPLATE");
		armors.add("GOLD_BOOTS");
		armors.add("DIAMOND_HELMET");
		armors.add("DIAMOND_LEGGINGS");
		armors.add("DIAMOND_CHESTPLATE");
		armors.add("DIAMOND_BOOTS");
		armors.add("PUMPKIN");

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
