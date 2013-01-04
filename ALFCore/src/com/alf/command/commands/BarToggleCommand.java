package com.alf.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

public class BarToggleCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public BarToggleCommand(AlfCore plugin) {
		super("Toggle mana/exp bar output.");
		this.plugin = plugin;
		setDescription("Switches between mana/exp display through the exp bar.");
		setUsage("/alf display");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "alf display", "alf disp" });
	}

	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (cs instanceof ConsoleCommandSender)
			return false;
		
		Alf alf = this.plugin.getCharacterManager().getAlf((Player)cs);
		alf.toggleDisplayBar();
		
		Messaging.send(cs, "Display bar toggled.", new Object[0]);
		
		return true;
	}

	
}
