package com.alf.chat.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * Leave all channel audiences.
 * @author Eteocles
 */
public class LeaveAllChannelCommand extends BasicCommand {

	private final AlfChat plugin;
	
	public LeaveAllChannelCommand(AlfChat plugin) {
		super("Leave All Channels");
		this.plugin = plugin;
		setDescription("Leave all listening and talking channels.");
		setUsage("/ch leaveall");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] {"ch leaveall", "ch stfu" });
	}
	
	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (! (cs instanceof Player))
			return false;
		
		ChPlayer player = this.plugin.getChatManager().getChPlayer((Player) cs);
		if (player.hasChannels()) {
			for (String s : player.getChannels()) {
				this.plugin.getChatManager().removePlayerFromChannelAudience((Player)cs, s);
			}
			player.setMainChannel(null);
			Messaging.send(cs, "You have left all of your channels!", new Object[0]);
		} else {
			Messaging.send(cs, "You are not listening to any channels!", new Object[0]);
		}
		
		return true;
	}

}
