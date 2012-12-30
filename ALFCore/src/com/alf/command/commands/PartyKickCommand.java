package com.alf.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.event.AlfLeavePartyEvent;
import com.alf.character.Alf;
import com.alf.character.party.AlfParty;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command to kick a player from a party.
 * @author Eteocles
 */
public class PartyKickCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public PartyKickCommand(AlfCore plugin) {
		super("Party Kick");
		this.plugin = plugin;
		setDescription("Kicks a player from the party");
		setUsage("/party kick §9<player>");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] { "party kick" });
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

		if (party.getLeader() == null || !party.getLeader().equals(alf)) {
			Messaging.send(sender, "You must be the leader to kick players from the party.", new Object[0], ChatColor.RED);
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
		for (Alf found : party.getMembers())
			if (alf.getPlayer().equals(tPlayer)) {
				tAlf = found;
				break;
			}
		
		if (tAlf == null) {
			Messaging.send(sender, "That player is not in your party.", new Object[0], ChatColor.RED);
			return true;
		}
		
		AlfParty alfParty = alf.getParty();

		AlfLeavePartyEvent event = new AlfLeavePartyEvent(alf, alfParty, AlfLeavePartyEvent.LeavePartyReason.KICK);
		this.plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			sender.sendMessage("You can not kick the player from the party at this time!");
			return true;
		}
		alfParty.messageParty("$1 has been kicked from the party", new Object[] { player.getDisplayName() });
		alfParty.removeMember(tAlf);
		return true;
	}

}
