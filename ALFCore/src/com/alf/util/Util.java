package com.alf.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.v1_4_6.EntityLiving;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockIterator;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.Pet;
import com.alf.character.effect.common.PetFollowEffect;

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
	public static final List<String> woodWeps;
	public static final List<String> goldWeps;
	public static final List<String> ironWeps;
	public static final List<String> diamondWeps;

	public static final HashMap<String, Location> deaths = new LinkedHashMap<String, Location>() {
		private static final long serialVersionUID = -5160276589164566330L;
		private static final int MAX_ENTRIES = 50;
		protected boolean removeEldestEntry(Map.Entry<String, Location> eldest) {
			return size() > MAX_ENTRIES;
		}
	};
	
	/**
	 * Check whether a path of blocks is clear to charge through.
	 * @param world - world to check in
	 * @param loc1 - block in path
	 * @param loc2 - last block in path
	 * @param offset - vertical y offset
	 * @param blocks - number of blocks to check
	 * @return whether the path is blocked or not
	 */
	public static boolean isWayBlocked(World world, Location loc1, Location loc2, double offset, int blocks) {
		boolean blocked = false;
		BlockIterator bi = new BlockIterator(world, 
				loc1.toVector(), 
				loc2.subtract(loc1).toVector(), 
				offset, blocks);
		for (Block b = bi.next(); b != null;) {
			if (! Util.transparentBlocks.contains(b.getType()))
				blocked = true;
			if (bi.hasNext())
				b = bi.next();
			else b = null;
		}
		return blocked;
	}

	/**
	 * Spawn a pet at a given location.
	 * @param plugin
	 * @param alf
	 * @param petType
	 * @param location
	 * @return
	 */
	public static Pet spawnPet(AlfCore plugin, Alf alf, EntityType petType, Location location) {
		World w = location.getWorld();

		//		AlfCore.log(Level.INFO, "Creating pet of type  " + petType.getName() + " in world " + w + " at location [" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]");
		LivingEntity petEntity = (LivingEntity) w.spawnEntity(location, petType);

		Player player = alf.getPlayer();

		if (petEntity instanceof Ageable) { 
			((Ageable)petEntity).setBaby();
			((Ageable)petEntity).setAgeLock(true);
		}

		if (petEntity instanceof Creature)
			((Creature)petEntity).setTarget(player);

		if (petEntity instanceof Slime)
			((Slime)petEntity).setSize(1);

		EntityLiving eL = ((CraftLivingEntity)petEntity).getHandle();
		//Clear the PathEntity (PathEntity pe = null)
		eL.getNavigation().g();

		Pet pet = new Pet(plugin, petEntity, player.getName() + "'s Pet", alf);
		alf.setPet(pet);

		plugin.getCharacterManager().addPet(pet);

		alf.addEffect(new PetFollowEffect(null, 250L));

		return pet;
	}

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
		
		if (slot != -1)
			inv.clear(slot);
		Messaging.send(player, "You are not trained to use a $1.", new Object[] { MaterialUtil.getFriendlyName(item.getType()) });
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
	 * Get the feather fall level of an inventory.
	 * @param inv
	 * @return
	 */
	public static int getFeatherFallLevel(PlayerInventory inv) {
		int level = 0;

		for (ItemStack armor : inv.getArmorContents())
			if (armor != null && armor.containsEnchantment(Enchantment.PROTECTION_FALL))
				level += armor.getEnchantmentLevel(Enchantment.PROTECTION_FALL);

		return level;
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
			return true;
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
			return (Integer) val;
		if (val instanceof Double)
			return ((Double) val).intValue();
		if (val instanceof Float)
			return ((Float) val).intValue();
		if (val instanceof Long)
			return ((Long) val).intValue();
		if (val instanceof BigDecimal)
			return (((BigDecimal)val).intValue());
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
			return Double.valueOf((Integer)val);
		if (val instanceof Double)
			return (Double)val;
		if (val instanceof Float)
			return Double.valueOf(((Float)val));
		if (val instanceof Long)
			return Double.valueOf(((Long)val));
		if (val instanceof BigDecimal)
			return (((BigDecimal)val).doubleValue());
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

	/**
	 * Format the double by fixing its precision.
	 * @param d
	 * @return
	 */
	public static double formatDouble(double d) {
		int val = (int) (d * 1000.0D);
		return val / 1000.0D;
	}

	/**
	 * 
	 * @param yaw
	 * @param pitch - rotation about the x axis (-90 is straight up, 90 is straight down // inverted)
	 * @param magnitude
	 * @return
	 */
	public static double[] toCartesian(double yaw, double pitch, int magnitude) {
//		double pitchr= Math.toRadians(pitch);
		double yawr = Math.toRadians(yaw);
		double x = magnitude*- Math.sin(yawr);
		//Invert the sin value for pitch because sin is an odd function.
		double y = -magnitude*Math.sin(pitch);
		double z = magnitude*Math.cos(yawr);
		return new double[] {x, y, z};
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
		
		woodWeps = new ArrayList<String>(5);
		woodWeps.add("WOOD_SWORD");
		woodWeps.add("WOOD_AXE");
		woodWeps.add("WOOD_SPADE");
		woodWeps.add("WOOD_PICKAXE");
		woodWeps.add("WOOD_HOE");
		
		goldWeps = new ArrayList<String>(5);
		goldWeps.add("GOLD_SWORD");
		goldWeps.add("GOLD_AXE");
		goldWeps.add("GOLD_SPADE");
		goldWeps.add("GOLD_PICKAXE");
		goldWeps.add("GOLD_HOE");
		
		ironWeps = new ArrayList<String>(5);
		ironWeps.add("IRON_SWORD");
		ironWeps.add("IRON_AXE");
		ironWeps.add("IRON_SPADE");
		ironWeps.add("IRON_PICKAXE");
		ironWeps.add("IRON_HOE");
		
		diamondWeps = new ArrayList<String>(5);
		diamondWeps.add("DIAMOND_SWORD");
		diamondWeps.add("DIAMOND_AXE");
		diamondWeps.add("DIAMOND_SPADE");
		diamondWeps.add("DIAMOND_PICKAXE");
		diamondWeps.add("DIAMOND_HOE");
		
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
		transparentBlocks.add(Material.LONG_GRASS);

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
		transparentIds.add((byte)Material.LONG_GRASS.getId());
	}

}
