package com.alf.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

public class AdminEnergyCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public AdminEnergyCommand(AlfCore plugin) {
		super("AdminManaCommand");
		this.plugin = plugin;
		setDescription("Sets a user's mana");
		setUsage("/alf admin mana §9<player> <mana>");
		setArgumentRange(0,2);
		setIdentifiers(new String[] { "alf admin mana", "aelf admin m" });
		setPermission("alf.admin.health");
	}

	/**
	 * Execute the command.
	 */
	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {

		if (args.length == 0) {
			Alf alf = this.plugin.getCharacterManager().getAlf((Player)cs);
			alf.setMana(alf.getMaxMana());
			
			Messaging.send(cs, "Your mana has been restored!", new Object[0]);
			
			return true;
		} else if (args.length == 1) {
			Player player = this.plugin.getServer().getPlayer(args[0]);

			if (player == null) {
				Messaging.send(cs, "Failed to find a matching Player for $1. Offline players are not supported!", new Object[] { 
						args[0] }, ChatColor.RED);
				return false;
			}

			Alf alf = this.plugin.getCharacterManager().getAlf(player);
			alf.setMana(alf.getMaxMana());
			
			Messaging.send(player, "The blessed gods have restored your mana!", new Object[0]);
			Messaging.send(cs, "You have restored $1's mana!", new Object[] { args[0] });

			return true;
		} 
		
		Player player = this.plugin.getServer().getPlayer(args[1]);

		if (player == null) {
			Messaging.send(cs, "Failed to find a matching Player for $1. Offline players are not supported!", new Object[] { 
					args[0] }, ChatColor.RED);
			
			return false;
		}

		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		int mana = alf.getMana();
		try {
			mana = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			Messaging.send(cs, "Invalid mana.", new Object[0], ChatColor.RED);
			return false;
		}
		
		alf.setMana(mana);
		
		Messaging.send(player, "The gods have set your mana to $1!", new Object[] { mana });
		Messaging.send(cs, "You have set $1's mana to: $2!", new Object[] { args[1], mana });

		return true;
	}
}
