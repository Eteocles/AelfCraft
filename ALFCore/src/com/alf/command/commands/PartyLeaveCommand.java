package com.alf.command.commands;

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
 * A command to leave a party.
 * @author Eteocles
 */
public class PartyLeaveCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public PartyLeaveCommand(AlfCore plugin) {
		super("Party Leave");
		this.plugin = plugin;
		setDescription("Leaves your party");
		setUsage("/party leave");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "party leave" });
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
			Messaging.send(sender, "You don't have a party!", new Object[0], ChatColor.RED);
			return true;
		}
		AlfParty alfParty = alf.getParty();

		AlfLeavePartyEvent event = new AlfLeavePartyEvent(alf, alfParty, AlfLeavePartyEvent.LeavePartyReason.COMMAND);
		this.plugin.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			sender.sendMessage("You can not leave the party at this time!");
			return true;
		}
		
		alfParty.messageParty("$1 has left the party", new Object[] { player.getName() });
		alfParty.removeMember(alf);
		if (alfParty.getMembers().size() == 0)
			this.plugin.getPartyManager().removeParty(alfParty);
		
		alf.setParty(null);
		return true;
	}

}
