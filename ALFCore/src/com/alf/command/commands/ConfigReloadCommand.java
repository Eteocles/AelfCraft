package com.alf.command.commands;

import org.bukkit.command.CommandSender;

import com.alf.AlfCore;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command that reloads configuration.
 * @author Eteocles
 */
public class ConfigReloadCommand extends BasicCommand {

	private AlfCore plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public ConfigReloadCommand(AlfCore plugin) {
		super("Reload");
		this.plugin = plugin;
		setDescription("Reloads the AlfCore config file");
		setUsage("/alf admin reload");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "alf admin reload" , "aelf admin reload"});
		setPermission("alf.admin.reload");
	}
	
	/**
	 * Execute the command.
	 */
	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		
		if (this.plugin.getConfigManager().reload())
			Messaging.send(cs, "Configs reloaded.", new Object[0]);
		
		return true;
	}

}
