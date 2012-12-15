package com.alf.command.commands;

import java.util.*;

import org.bukkit.command.CommandSender;

import com.alf.AlfPlugin;
import com.alf.command.BasicCommand;
import com.alf.command.Command;
import com.alf.command.CommandHandler;

/**
 * Describes a HelpCommand, which displays commands with listed information.
 * @author Eteocles
 */
public class HelpCommand extends BasicCommand {

	private static final int CMDS_PER_PAGE = 8;
	private final AlfPlugin plugin;
	
	/**
	 * Construct a HelpCommand.
	 * @param plugin
	 */
	public HelpCommand(AlfPlugin plugin) {
		super("Help");
		this.plugin = plugin;
		setDescription("Displays the help menu");
		setUsage("/alf help §8[page#]");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] { "alf", "alf help", "aelf", "aelf help" });
	}

	/**
	 * Execute the help command.
	 * Display all commands that are flagged for permission and help menu display.
	 * @param sender
	 * @param msg
	 * @param args
	 * @return
	 */
	@Override
	public boolean execute(CommandSender sender, String msg, String[] args) {
		int page = 0;
		if (args.length != 0)
			try {
				page = Integer.parseInt(args[0]) - 1;
			} catch (NumberFormatException e) {
			}
		List<Command> sortCommands = this.plugin.getCommandHandler().getCommands();
		List<Command> commands = new ArrayList<Command>();
		
		for (Command command : sortCommands) {
			if (command.isShownOnHelpMenu() && CommandHandler.hasPermission(sender, command.getPermission()))
				commands.add(command);
		}
		
		int numPages = commands.size() / CMDS_PER_PAGE;
		
		if (commands.size() % CMDS_PER_PAGE != 0) 
			numPages++;
		
		if (page >= numPages || page < 0)
			page = 0;
		
		sender.sendMessage("§c-----[ §fAelfCraft Help <" + (page + 1) + "/" + numPages + ">§c ]-----");
		
		int start = page*8;
		int end = start + 8;
		if (end > commands.size())
			end = commands.size();
		
		for (int c = start; c < end; c++) {
			Command cmd = (Command) commands.get(c);
			sender.sendMessage(" §a" + cmd.getUsage());
		}
		
		sender.sendMessage("§cFor more info on a particular command, type §f/<command> ?");
		return true;
	}

}
