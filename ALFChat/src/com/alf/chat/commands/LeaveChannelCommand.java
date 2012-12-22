package com.alf.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;
import com.alf.chat.ChatManager;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * Leave a given channel.
 * @author Eteocles
 */
public class LeaveChannelCommand extends BasicCommand {

	private final AlfChat plugin;
	
	public LeaveChannelCommand(AlfChat plugin) {
		super("Leave Channel");
		this.plugin = plugin;
		setDescription("Stop listening to a channel. Also leaves channel if main channel.");
		setUsage("/ch leave §8[channel name]");
		setArgumentRange(1, 1);
		setIdentifiers(new String[] {"ch leave"});
	}
	
	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (! (cs instanceof Player))
			return true;
		if (args.length != 0) {
			Player p = (Player) cs;
			ChatManager cm = plugin.getChatManager();
			String channel = cm.getFullChannelName(args[0]);
			ChPlayer player = cm.getChPlayer(p);
			
			if (player.getMainChannel() != null && player.getMainChannel().equalsIgnoreCase(channel)) {
				//Main channel, then remove from audience and from main.
				cm.removePlayerFromChannelAudience(p, channel);
				player.setMainChannel(null);
				Messaging.send(cs, "You are no longer talking in the channel " + channel, new Object[0]);
			}
			else if (cm.removePlayerFromChannelAudience(p, channel)) {
				Messaging.send(cs, "You have stopped listening to the channel " + channel, new Object[0]);
			}
			else Messaging.send(cs, "That channel does not exist!", new Object[0], ChatColor.RED);
 		}
		return true;
	}

}
