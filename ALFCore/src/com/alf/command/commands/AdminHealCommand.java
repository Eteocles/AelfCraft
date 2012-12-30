package com.alf.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command to heal a player.
 * @author Eteocles
 */
public class AdminHealCommand extends BasicCommand {

	private final AlfCore plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public AdminHealCommand(AlfCore plugin) {
		super("AdminHeal");
		this.plugin = plugin;
		setDescription("Heals an alf to full health.");
		setUsage("/alf admin heal §9<player>");
		setArgumentRange(0, 3);
		setIdentifiers(new String[] { "alf admin heal", "aelf admin heal" });
		setPermission("alf.admin.heal");
	}
	
	/**
	 * Execute the command.
	 */
	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (args.length > 0) {
			List<Player> players = this.plugin.getServer().matchPlayer(args[0]);
			if (players.isEmpty()) {
				Messaging.send(cs, "No player named $1 was found!", new Object[] { args[0] }, ChatColor.RED);
				return true;
			}
			String names = "";
			for (Player player : players) {
				Alf alf = this.plugin.getCharacterManager().getAlf(player);
				alf.setHealth(alf.getMaxHealth());
				alf.syncHealth();
				Messaging.send(player, "You have been healed by the blessed gods!", new Object[0]);
				names += player.getDisplayName() + " ";
			}
			Messaging.send(cs, "You have restored: $1to full health.", new Object[] { names } );
			return true;
		} if (cs instanceof ConsoleCommandSender) {
			Messaging.send(cs, "You must specify a player to heal!", new Object[0], ChatColor.RED);
		}
		
		Alf alf = this.plugin.getCharacterManager().getAlf((Player) cs);
		alf.setHealth(alf.getMaxHealth());
		alf.syncHealth();
		Messaging.send(cs, "You have been restored to full health!", new Object[0]);
		return true;
	}

}
