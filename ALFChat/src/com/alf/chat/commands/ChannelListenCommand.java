package com.alf.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * Listen to a channel's conversations.
 * @author Eteocles
 */
public class ChannelListenCommand extends BasicCommand {

	private final AlfChat plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public ChannelListenCommand(AlfChat plugin) {
		super("Channel Listen");
		this.plugin = plugin;
		setDescription("Lets the user read a channel.");
		setUsage("/ch l §8[channel name]");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] { "ch l", "ch listen" });
	}
	
	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (! (cs instanceof Player))
			return false;
		if (args.length != 0) {
			String channel = args[0];
			if (this.plugin.getChatManager().addPlayerToChannelAudience((Player) cs, channel)) {
				Messaging.send(cs, "You are now listening to channel " + 
					plugin.getChatManager().getFullChannelName(channel), new Object[0]);
			} else if (plugin.getChatManager().getChannel(channel) == null) Messaging.send(cs, "That channel does not exist!", 
					new Object[0], ChatColor.RED);
		} else {
			Messaging.send(cs, plugin.getChatManager().getChannelList((Player) cs), new Object[0], ChatColor.DARK_AQUA);
		}
		return true;
	}

}
