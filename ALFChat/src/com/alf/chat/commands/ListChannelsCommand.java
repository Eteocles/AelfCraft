package com.alf.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * List all channels the player is in.
 * @author Eteocles
 */
public class ListChannelsCommand extends BasicCommand {

	private final AlfChat plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public ListChannelsCommand(AlfChat plugin) {
		super("List My Channels");
		this.plugin = plugin;
		setDescription("List all channels you are listening to.");
		setUsage("/mych §8[channel name]");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] {"mych", "mychannels"});
	}
	
	/**
	 * Execute the command.
	 */
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (! (cs instanceof Player))
			return false;
		ChPlayer player = plugin.getChatManager().getChPlayer((Player) cs);
		String channelList = "Your chat channels: ";
		if (player.hasChannels()) {
			for (String channel : player.getChannels()) {
				if (player.getMainChannel() != null && player.getMainChannel().equals(channel))
					channelList += ChatColor.DARK_AQUA + channel + " " + ChatColor.AQUA;
				else channelList += channel + " ";
			}
			Messaging.send(cs, channelList, new Object[0], ChatColor.AQUA);
		} else Messaging.send(cs, "You are currenly not in any channels.", new Object[0]);
		return true;
	}

}
