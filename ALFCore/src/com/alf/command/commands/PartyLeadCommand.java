package com.alf.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.party.AlfParty;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command to set/check the leader of a party.
 * @author Eteocles
 */
public class PartyLeadCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public PartyLeadCommand(AlfCore plugin) {
		super("Party Lead");
		this.plugin = plugin;
		setDescription("Checks or sets the leader of the party.");
		setUsage("/party lead §9<player>");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] { "party lead", "party leader" });
	}

	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender sender, String identifier, String[] args) {
		if (!(sender instanceof Player))
			return false;

		Player player = (Player)sender;
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		AlfParty party = alf.getParty();
		
		if (party == null) {
			Messaging.send(sender, "You don't have a party!", new Object[0], ChatColor.RED);
			return true;
		}
		
		if (args.length == 0) {
			Messaging.send(sender, "The current party leader is $1.", new Object[] { party.getLeader().getPlayer().getDisplayName() });
			return true;
		}

		if (!party.getLeader().equals(alf)) {
			Messaging.send(sender, "Only the leader of a party can change give leadership to another alf!", new Object[0], ChatColor.RED);
			return true;
		}

		Player tPlayer = Bukkit.getPlayer(args[0]);
		if (tPlayer == null) {
			Messaging.send(sender, "That player is not in your party.", new Object[0], ChatColor.RED);
			return true;
		}
		
		Alf tAlf = null;
		
		for (Alf found : party.getMembers()) {
			if (alf.getPlayer().equals(tPlayer)) {
				tAlf = found;
				break;
			}
		}
		
		if (tAlf == null) {
			Messaging.send(sender, "That player is not in your party.", new Object[0], ChatColor.RED);
			return true;
		}
		party.setLeader(tAlf);
		party.messageParty("$1 is now the party leader.", new Object[] { tAlf.getPlayer().getDisplayName() });
		return true;
	}

}
