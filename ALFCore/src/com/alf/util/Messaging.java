package com.alf.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.alf.AlfPlugin;

/**
 * Messaging Utility for AelfCraft.
 * @author Eteocles
 * @todo add mana bar and experience bar support
 */
public class Messaging {
	
	/**
	 * Generate a server broadcast of a given message and parameters.
	 * @param plugin
	 * @param msg
	 * @param params
	 */
	public static void broadcast(AlfPlugin plugin, String msg, Object[] params) {
		plugin.getServer().broadcastMessage(parameterizeMessage(msg, params, ChatColor.GRAY));
	}
	
	/**
	 * Create a text representation of a full health bar.
	 * @param health
	 * @param maxHealth
	 * @return
	 */
	public static String createFullHealthBar(double health, double maxHealth) {
		return ("§aHP: §f"+((int)Math.ceil(health)) + "/" + ((int)Math.ceil(maxHealth)) + " "
				+ createHealthBar(health, maxHealth));
	}
	
	/**
	 * Create a text representation of a health bar.
	 * @param health - health remaining
	 * @param maxHealth - maximum health
	 * @return
	 */
	public static String createHealthBar(double health, double maxHealth) {
		String healthBar = ChatColor.RED + "[" + ChatColor.GREEN;
		int progress = (int) (health / maxHealth * 50.0D);
		//Display remaining health.
		for (int i = 0; i < progress; i++)
			healthBar += "|";
		//Display empty health bars.
		healthBar += ChatColor.DARK_RED;
		for (int i = 0; i < 50 - progress; i++)
			healthBar += "|";
		healthBar += ChatColor.RED + "]";
		return healthBar + " - " + ChatColor.GREEN + ((int) health / maxHealth * 100.0D) + "%";
	}
	
	/**
	 * Create a text representation of a mana bar.
	 * @param mana
	 * @param maxMana
	 * @return
	 */
	public static String createManaBar(int mana, int maxMana) {
		String manaBar = ChatColor.RED + "[" + ChatColor.AQUA;
		int percent = (int) (((double) mana / (double) maxMana) * 100.0D);
		int progress = percent / 2;
		for (int i = 0; i < progress; i++)
			manaBar += '|';
		manaBar += ChatColor.DARK_BLUE;
		for (int i = 0; i < 50 - progress; i++)
			manaBar += '|';
		manaBar += ChatColor.RED + "]";
		return manaBar + " - " + ChatColor.BLUE + "%";
	}
	
	/**
	 * Get the Living Entity friendly name of a living entity.
	 * @param lEntity
	 * @return
	 */
	@SuppressWarnings("incomplete-switch")
	public static String getLivingEntityName(LivingEntity lEntity) {
		switch (lEntity.getType()) {
			case PLAYER:
				return ((Player)lEntity).getDisplayName();
			case BAT:
				return "Bat";
			case BLAZE:
				return "Blaze";
			case CAVE_SPIDER:
				return "Cave Spider";
			case MUSHROOM_COW:
				return "Mushroom Cow";
			case COW:
				return "Cow";
			case CHICKEN:
				return "Chicken";
			case CREEPER:
				return "Creeper";
			case ENDER_DRAGON:
				return "Ender Dragon";
			case ENDERMAN:
				return "Enderman";
			case GHAST:
				return "Ghast";
			case GIANT:
				return "Giant";
			case IRON_GOLEM:
				return "Iron Golem";
			case MAGMA_CUBE:
				return "Magma Cube";
			case OCELOT:
				return "Ocelot";
			case PIG:
				return "Pig";
			case PIG_ZOMBIE:
				return "Pig Zombie";
			case SHEEP:
				return "Sheep";
			case SKELETON:
				return "Skeleton";
			case SILVERFISH:
				return "Silverfish";
			case SLIME:
				return "Slime";
			case SNOWMAN:
				return "Snowman";
			case SPIDER:
				return "Spider";
			case SQUID:
				return "Squid";
			case VILLAGER:
				return "Villager";
			case WITCH:
				return "Witch";
			case WITHER:
				return "Wither";
			case WOLF:
				return "Wolf";
			case ZOMBIE:
				return "Zombie";
		}
		return null;
	}
	
	/**
	 * Send a message to a player.
	 * @param player
	 * @param msg
	 * @param params
	 */
	public static void send(CommandSender player, String msg, Object[] params) {
		player.sendMessage(parameterizeMessage(msg, params, ChatColor.GRAY));
	}
	
	public static void send(CommandSender player, String msg, Object[] params, ChatColor col) {
		player.sendMessage(parameterizeMessage(msg, params, col));
	}
	
	/**
	 * Parameterize a message using regex patterns [$num]
	 * @param msg - message to be parameterized
	 * @param params - parameters to be replaced
	 * @param col - color to be used
	 * @return - parameterized string
	 */
	public static String parameterizeMessage(String msg, Object[] params, ChatColor col) {
		msg = col + msg;
		if (params != null)
			for (int i = 0; i < params.length; i++)
				msg = msg.replace("$"+(i+1), params[i].toString());
		return msg;
	}

}
