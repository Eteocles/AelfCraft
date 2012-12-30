package com.alf.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.event.AlfJoinPartyEvent;
import com.alf.character.Alf;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command that allows a player to accept an invitation.
 * @author Eteocles
 */
public class PartyAcceptCommand extends BasicCommand {

	private final AlfCore plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public PartyAcceptCommand(AlfCore plugin) {
		super("Party Accept");
		this.plugin = plugin;
		setDescription("Accept a party invite");
	    setUsage("/party accept §9<player>");
	    setArgumentRange(1, 1);
	    setIdentifiers(new String[] { "party accept" });
	}
	
	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender sender, String identifier, String[] args) {
		if (! (sender instanceof Player))
			return false;
		
		Player player = (Player)sender;
	    Alf alf = this.plugin.getCharacterManager().getAlf(player);
	    
	    if (this.plugin.getServer().getPlayer(args[0]) != null) {
	    	
	      Player newPlayer = this.plugin.getServer().getPlayer(args[0]);
	      Alf newAlf = this.plugin.getCharacterManager().getAlf(newPlayer);
	      
	      if (alf.getParty() != null) {
	        Messaging.send(player, "Sorry, you're already in a party", new Object[0], ChatColor.RED);
	        return false;
	      }
	      
	      if (newAlf.getParty() != null && newAlf.getParty().isInvited(player.getName())) {
	        AlfJoinPartyEvent event = new AlfJoinPartyEvent(alf, newAlf.getParty());
	        this.plugin.getServer().getPluginManager().callEvent(event);
	        
	        if (event.isCancelled())
	          return false;
	        
	        alf.setParty(newAlf.getParty());
	        newAlf.getParty().addMember(alf);
	        alf.getParty().messageParty("$1 has joined the party", new Object[] { player.getName() });
	        Messaging.send(player, "You're now in $1's party", new Object[] { newPlayer.getName() });
	        
	        alf.getParty().removeInvite(player);
	      } 
	      else
	        Messaging.send(player, "Sorry, $1 hasn't invited you to their party", new Object[] { newPlayer.getName() }, ChatColor.RED);
	    } 
	    else
	      Messaging.send(player, "Sorry, $1 doesn't match anyone in-game", new Object[] { args[0] }, ChatColor.RED);

		
		return true;
	}
	
}
