package com.alf.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command to set a player's health.
 * @author Eteocles
 */
public class AdminHealthCommand extends BasicCommand {

	private final AlfCore plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public AdminHealthCommand(AlfCore plugin) {
		super("AdminHealthCommand");
		this.plugin = plugin;
		setDescription("Sets a user's health");
		setUsage("/alf admin hp §9<player> <health>");
		setArgumentRange(2,2);
		setIdentifiers(new String[] { "alf admin hp", "aelf admin hp" });
		setPermission("alf.admin.health");
	}
	
	/**
	 * Execute the command.
	 */
	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		Player player = this.plugin.getServer().getPlayer(args[0]);
		
		if (player == null) {
			Messaging.send(cs, "Failed to find a matching Player for $1. Offline players are not supported!", new Object[] { args[0] }, ChatColor.RED);
			return false;
		}
		
		int health;
		try {
			health = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			Messaging.send(cs, "Invalid health.", new Object[0], ChatColor.RED);
			return false;
		}
		
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		alf.setHealth(health);
		alf.syncHealth();
		
		Messaging.send(cs, "You have successfully changed $1's health.", new Object[] { player.getName() });
		
		return true;
	}

}
