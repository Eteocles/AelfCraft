package com.alf.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.party.AlfParty;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command to toggle party modes.
 * @author Eteocles
 */
public class PartyModeCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public PartyModeCommand(AlfCore plugin) {
		super("Party Mode");
		this.plugin = plugin;
		setDescription("Toggles exp sharing or party pvp");
		setUsage("/party mode §9<pvp|exp>");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] { "party mode" });
	}

	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender sender, String identifier, String[] args) {
		if (!(sender instanceof Player))
			return false;
		
		Player player = (Player)sender;
		Alf alf = this.plugin.getCharacterManager().getAlf(player);

		if (alf.getParty() == null) {
			Messaging.send(player, "You are not in a party.", new Object[0], ChatColor.RED);
			return false;
		}

		AlfParty alfParty = alf.getParty();
		if (alfParty.getLeader().equals(alf)) {
			if (args[0].equalsIgnoreCase("pvp"))
				alfParty.pvpToggle();
			else if (args[0].equalsIgnoreCase("exp")) {
				alfParty.expToggle();
			}
			return true;
		}
		Messaging.send(player, "Sorry, you need to be the leader to do that", new Object[0], ChatColor.RED);
		return false;
	}
}
