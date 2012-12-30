package com.alf.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * Describes a ShushCommand, which toggles verbose mode for a player.
 * @author Eteocles
 */
public class ShushCommand extends BasicCommand {

	private final AlfCore plugin;
	
	/**
	 * Construct the ShushCommand.
	 * @param plugin
	 */
	public ShushCommand(AlfCore plugin) {
		super("Shush");
		this.plugin = plugin;
		setDescription("Toggles message output for Aelfs.");
		setUsage("/shush");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "shush", "stfu", "quiet" });
	}
	
	/**
	 * Execute the Shush command.
	 * @param sender
	 * @param msg
	 * @param args
	 * @return
	 */
	public boolean execute(CommandSender sender, String msg, String[] args) {
		
		Alf alf = plugin.getCharacterManager().getAlf((Player) sender);
		boolean verbose = alf.isVerbose();
		
		if (verbose)
			Messaging.send(sender, "Aelf output disabled.", new Object[0]);
		else 
			Messaging.send(sender, "Aelf output enabled.", new Object[0]);
		
		alf.setVerbose(! verbose);
		
		return true;
	}
	
}
