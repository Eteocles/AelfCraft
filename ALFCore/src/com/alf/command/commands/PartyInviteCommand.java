package com.alf.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.event.PartyInviteEvent;
import com.alf.character.Alf;
import com.alf.character.party.AlfParty;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command to invite a player to a party.
 * @author Eteocles
 */
public class PartyInviteCommand extends BasicCommand {

	private final AlfCore plugin;

	public PartyInviteCommand(AlfCore plugin) {
		super("Party Invite");
		this.plugin = plugin;
		setDescription("Invites a player to your party");
		setUsage("/party invite §9<player>");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] { "party invite" });
	}

	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender sender, String identifier, String[] args) {
		if (!(sender instanceof Player)) {
			Messaging.send(sender, "Only players can create parties", new Object[0], ChatColor.RED);
			return false;
		}

		Player player = (Player)sender;
		Player target = this.plugin.getServer().getPlayer(args[0]);
		Alf alf = this.plugin.getCharacterManager().getAlf(player);

		if (target == null) {
			Messaging.send(player, "Player not found.", new Object[0], ChatColor.RED);
			return false;
		}

		if (target.equals(player)) {
			Messaging.send(player, "You cannot invite yourself.", new Object[0], ChatColor.RED);
			return false;
		}

		if (alf.getParty() == null) {
			AlfParty newParty = new AlfParty(alf, this.plugin);
			this.plugin.getPartyManager().addParty(newParty);
			alf.setParty(newParty);
			Messaging.send(player, "Your party has been created", new Object[0]);
		}

		AlfParty party = alf.getParty();
		if (party.getLeader() == null || !party.getLeader().equals(alf)) {
			Messaging.send(player, "You are not leader of this party.", new Object[0], ChatColor.RED);
			return false;
		}

		int memberCount = party.getMembers().size();

		if (memberCount >= AlfCore.properties.maxPartySize) {
			Messaging.send(player, "Your party is full.", new Object[0], ChatColor.RED);
			return false;
		}

		if (memberCount + party.getInviteCount() >= AlfCore.properties.maxPartySize) {
			party.removeOldestInvite();
		}

		party.addInvite(target.getName());
		Bukkit.getPluginManager().callEvent(new PartyInviteEvent(this.plugin.getCharacterManager().getAlf(target), party));
		Messaging.send(target, "$1 has invited you to their party", new Object[] { player.getName() });
		Messaging.send(target, "Type /party accept $1 to join", new Object[] { player.getName() });
		Messaging.send(player, "$1 has been invited to your party", new Object[] { target.getName() });

		return true;
	}

}
