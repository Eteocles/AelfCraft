package com.alf.command.commands;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.party.AlfParty;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command to see who is in a party.
 * @author Eteocles
 *
 */
public class PartyWhoCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public PartyWhoCommand(AlfCore plugin) {
		super("Party Who");
		this.plugin = plugin;
		setDescription("Lists your party members");
		setUsage("/party who");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "party who" });
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
			Messaging.send(player, "Sorry, you aren't in a party", new Object[0], ChatColor.RED);
			return false;
		}
		
		Messaging.send(player, "$1", new Object[] { partyNames(alf.getParty()).toString() });
		return true;
	}

	/**
	 * Get the set of all player names in a party.
	 * @param party
	 * @return
	 */
	public Set<String> partyNames(AlfParty party) {
		Set<String> names = new HashSet<String>();
		
		for (Alf partyMember : party.getMembers())
			names.add(partyMember.getPlayer().getName());
		
		return names;
	}

}
