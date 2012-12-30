package com.alf.command.commands;

import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.event.PartyChatEvent;
import com.alf.character.Alf;
import com.alf.character.party.AlfParty;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command to chat within party.
 * @author Eteocles
 *
 */
public class PartyChatCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public PartyChatCommand(AlfCore plugin) {
		super("Party Chat");
		this.plugin = plugin;
		setDescription("Sends messages to your party");
		setUsage("/party §9<msg> OR /p §9<msg>");
		setArgumentRange(1, 1000);
		setIdentifiers(new String[] { "pc", "p", "party" });
	}

	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender sender, String identifier, String[] args)
	{
		if (!(sender instanceof Player))
			return false;

		Player player = (Player)sender;
		Alf alf = this.plugin.getCharacterManager().getAlf(player);

		AlfParty party = alf.getParty();
		if (party == null) {
			Messaging.send(player, "You are not in a party.", new Object[0], ChatColor.RED);
			return false;
		}

		Set<Alf> partyMembers = party.getMembers();
		if (partyMembers.size() <= 1) {
			Messaging.send(player, "Your party is empty.", new Object[0], ChatColor.RED);
			return false;
		}

		String msg = "";
		for (String word : args) 
			msg += word + " ";
		
		String fullMsg = msg.trim();
		PartyChatEvent pce = new PartyChatEvent(alf, party, fullMsg);
		
		if (pce.isCancelled())
			return true;
		
		fullMsg = pce.getMessage();

		if (player.equals(party.getLeader()))
			fullMsg = "§a[Party] §e" + player.getDisplayName() + "§a:§3 " + fullMsg;
		else 
			fullMsg = "§a[Party] §7" + player.getDisplayName() + "§a:§3 " + fullMsg;

		for (Alf partyMember : partyMembers) {
			partyMember.getPlayer().sendMessage(fullMsg);
		}
		AlfCore.log(Level.INFO, fullMsg);
		return true;
	}

}
