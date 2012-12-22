package com.alf.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;
import com.alf.chat.channel.ChatChannel;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * Allow the user to see what members are listening to a channel.
 * @author Eteocles
 */
public class ChannelWhoCommand extends BasicCommand {

	private final AlfChat plugin;

	/**
	 * Constructs the command.
	 * @param plugin
	 */
	public ChannelWhoCommand(AlfChat plugin) {
		super("List Channel Members.");
		this.plugin = plugin;
		setDescription("Lets the user see who is in a channel.");
		setUsage("/ch who §8[channel name]");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] { "ch who", "ch players"});
	}
	
	/**
	 * Executes the command.
	 * @param cs
	 * @param msg
	 * @param args
	 * @return
	 */
	public boolean execute(CommandSender cs, String msg, String[] args) {
		//Channel specified.
		if (args.length != 0) {
			String ch = args[0];
			ChatChannel channel = this.plugin.getChatManager().getChannel(ch);
			if (channel != null && channel.playerHasPermission((Player) cs)) {
				String playerList = "Players in §1" + channel.getName() + ": §2";
				for (ChPlayer p : channel.getPlayers())
					playerList += p.getName() + ", ";
				playerList = playerList.substring(0, playerList.length() - 2);
				Messaging.send(cs, playerList, new Object[] { ChatColor.AQUA, ChatColor.DARK_AQUA } );
			} else {
				Messaging.send(cs, "That channel does not exist, or you do not have sufficient privileges.", 
						new Object[0], ChatColor.RED);
				return true;
			}
		} 
		//Current channel.
		else {
			if (! (cs instanceof Player))
				return true;
			String ch = args[0];
			ChatChannel channel = this.plugin.getChatManager().getChannel(ch);
			if (channel != null && channel.playerHasPermission((Player) cs)) {
				String playerList = "Players in §1" + channel.getName() + ": §2";
				for (ChPlayer p : channel.getPlayers())
					playerList += p.getName() + ", ";
				playerList = playerList.substring(0, playerList.length() - 2);
				Messaging.send(cs, playerList, new Object[] { ChatColor.AQUA, ChatColor.DARK_AQUA } );
			} else {
				Messaging.send(cs, "You are currently not speaking in a channel.", new Object[0]);
			}
		}
		return true;
	}

}
