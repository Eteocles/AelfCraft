package com.alf.chat.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.chat.AlfChat;
import com.alf.chat.ChPlayer;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * Toggle slow mode for a player(s)/
 * @author Eteocles
 */
public class ToggleSlowCommand extends BasicCommand {

	private final AlfChat plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public ToggleSlowCommand(AlfChat plugin) {
		super("Toggle SlowChat");
		this.plugin = plugin;
		setDescription("Toggle a player's slow-mode.");
		setUsage("/slow §8[player]");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] {"slow", "turtle" });
		setPermission("alfchat.slow");
	}
	
	/**
	 * Execute the command.
	 */
	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		Player player = (Player) cs;
		
		if (args.length != 0) {
			List<Player> players = Bukkit.getServer().matchPlayer(args[0]);
			
			if (players.isEmpty()) {
				Messaging.send(cs, "There are no matching players online!", new Object[0], ChatColor.RED);
				return true;
			}
			
			String playerList = "";
			for (Player p : players) {
				if (p.getName().startsWith(args[0])) {
					ChPlayer cplayer = this.plugin.getChatManager().getChPlayer(p);
					cplayer.setSlow(cplayer.isSlow());
					playerList += p.getDisplayName() + " ";
				}
			}
			Messaging.send(cs, "Slow mode toggled for $1.", new Object[] { playerList.substring(0, playerList.length() - 1) });
		} else {
			ChPlayer cplayer = this.plugin.getChatManager().getChPlayer(player);
			cplayer.setSlow(cplayer.isSlow());
		}
		return true;
	}

}
