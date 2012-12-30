package com.alf.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.chat.ChatManager;
import com.alf.chat.channel.ChatChannel;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * Allows a player to view a list of channels, to switch channels, or to talk in a specific channel.
 * @author Eteocles
 */
public class ChannelCommand extends BasicCommand {

	private final AlfChat plugin;
	
	public ChannelCommand(AlfChat plugin) {
		super("Channel");
		this.plugin = plugin;
		setDescription("General channel commands.");
		setUsage("/ch §8[channel name]");
		setArgumentRange(0, Integer.MAX_VALUE);
		setIdentifiers(new String[] {"ch", "ch talk"});
		setPermission("alfchat.channel");
	}
	
	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender cs, String identifier, String[] args) {
		if (! (cs instanceof Player))
			return false;
		
		Player player = (Player) cs;
		if (args.length != 0) {
			ChatManager cm = this.plugin.getChatManager();
			String channel = cm.getFullChannelName(args[0]);
			if (args.length > 1) {
				ChatChannel ch = cm.getChannel(channel);
				if (ch != null && (ch.getPermission().equals("*") || 
						(player).hasPermission(ch.getPermission()))) {
					//Force listen to the channel.
					cm.addPlayerToChannelAudience(player, channel);
					String message = "";
					for (int i = 1; i < args.length; i++)
						message += args[i] + " ";
					//Do not switch talking channel but send a message instead.
					ch.sendMessage(cm.getChPlayer(player), message, new Object[0]);
					if (! cm.getChPlayer(player).getChannels().contains(channel))
						Messaging.send(cs,  "You are now listening to channel " + channel, new Object[0]);
				} else if (ch == null) Messaging.send(cs, "That channel does not exist!", 
						new Object[0], ChatColor.RED);
					
			} 
			//Change main channels.
			else if (cm.addPlayerToChannelAudience((Player) cs, channel)) {
				String fullName = cm.getFullChannelName(channel);
				cm.getChPlayer(player).setMainChannel(fullName);
				Messaging.send(cs, "You are now talking in channel " + fullName, new Object[0]);
			} 
			//Invalid channel.
			else if (cm.getChannel(channel) == null) Messaging.send(cs, "That channel does not exist!", 
					new Object[0], ChatColor.RED);
		} else {
			Messaging.send(cs, plugin.getChatManager().getChannelList(player), new Object[0], ChatColor.DARK_AQUA);
		}
		return true;
	}

}
