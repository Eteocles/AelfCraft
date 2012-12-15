package com.alf.command;

import java.util.*;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.AlfPlugin;
import com.alf.skill.Skill;
import com.alf.util.Messaging;

/**
 * The CommandHandler parses input commands.
 * @author Eteocles
 */
public class CommandHandler {

	//Instance variables.
	protected LinkedHashMap<String, Command> commands;
	protected HashMap<String, Command> identifiers;
	private AlfPlugin plugin;
	
	/**
	 * Construct the Command Handler. 
	 * @param alfPlugin
	 */
	public CommandHandler(AlfPlugin alfPlugin) {
		this.plugin = alfPlugin;
		this.commands = new LinkedHashMap<String, Command>();
		this.identifiers = new HashMap<String, Command>();
	}

	/**
	 * Add a command to the CommandHandler.
	 * @param command
	 */
	public void addCommand(Command command) {
		this.commands.put(command.getName().toLowerCase(), command);
		for (String ident : command.getIdentifiers())
			this.identifiers.put(ident.toLowerCase(), command);
	}
	
	/**
	 * Display the command help for the given command.
	 * @param cmd - command in question
	 * @param sender - person to display help for 
	 */
	private void displayCommandHelp(Command cmd, CommandSender sender) {
		sender.sendMessage("§cCommand:§e "+cmd.getName());
		if (sender instanceof Player && cmd instanceof Skill) {
			sender.sendMessage("§c");
		} else sender.sendMessage("§cUsage:§e " + cmd.getUsage());

		if (cmd.getNotes() != null) 
			for (String note : cmd.getNotes())
				sender.sendMessage("§e"+note);
	}
	
	/**
	 * Get a command for a given identifier.
	 * @param ident
	 * @param executor
	 * @return the command object
	 */
	public Command getCmdFromIdent(String ident, CommandSender executor) {
		Skill skill = ((AlfCore)this.plugin).getSkillManager().getSkillFromIdent(ident, executor);
		if (skill != null)
			return skill;
		
		if (this.identifiers.get(ident.toLowerCase()) == null) {
			for (Command cmd : this.commands.values()) {
				if (cmd.isIdentifier(executor, ident))
					return cmd;
			}
		}
		return (Command)this.identifiers.get(ident.toLowerCase());
	}
	
	/**
	 * Get the command from its name.
	 * @param name
	 * @return
	 */
	public Command getCommand(String name) {
		return (Command)this.commands.get(name.toLowerCase());
	}
	
	/**
	 * Get a list of all the commands.
	 */
	public List<Command> getCommands() {
		return new ArrayList<Command>(this.commands.values());
	}
	
	/**
	 * Remove a command from the stored commands.
	 * @param command - command to be removed
	 */
	public void removeCommand(Command command) {
		this.commands.remove(command.getName().toLowerCase());
		for (String ident : command.getIdentifiers())
			this.identifiers.remove(ident.toLowerCase());
	}
	
	/**
	 * Check whether a command sender has a permission.
	 * A CommandSender from the console or an invalid permission will default to permission granted.
	 * @param sender - command sender
	 * @param permission - permission node
	 * @return - whether the sender has permission
	 */
	public static boolean hasPermission(CommandSender sender, String permission) {
		if (! (sender instanceof Player) || permission == null || permission.isEmpty())
			return true;
		Player player = (Player) sender;
		return (player.isOp() || AlfCore.perms.has(player, permission));
	}
	
	/**
	 * Handle the execution of a command.
	 * @param sender - Command's sender
	 * @param name - name of the command
	 * @param label - label for the command
	 * @param args - arguments for the command
	 * @return - 
	 */
	public boolean dispatch(CommandSender sender, String commandName, String label,
			String[] args) {
		//Parse through all of the arguments, starting with all inclusive and excluding leftwards.
		for (int argsIncluded = args.length; argsIncluded >= 0; argsIncluded--) {
			String identifier = commandName;
			for (int i = 0; i < argsIncluded; i++)
				identifier += ' ' + args[i];
			Command cmd = getCmdFromIdent(identifier, sender);
			if (cmd != null) {
				String realArgs[] = (String[]) Arrays.copyOfRange(args, argsIncluded, args.length);
				if (! cmd.isInProgress(sender)) {
					//Number of arguments is incorrect
					if (realArgs.length < cmd.getMinArguments() || realArgs.length > cmd.getMaxArguments()) {
						displayCommandHelp(cmd, sender);
						return true;
					}
					//Has argument, and first argument is a ?
					if (realArgs.length > 0 && realArgs[0].equals("?")) {
						displayCommandHelp(cmd, sender);
						return true;
					}
				}
				//No permissions
				if (! hasPermission(sender, cmd.getPermission())) {
					Messaging.send(sender, "Insufficient permission.", new Object[0]);
					return true;
				}
				cmd.execute(sender, identifier, realArgs);
				return true;
			}
		}
		
		if (label.equalsIgnoreCase("skill")) {
			Messaging.send(sender, "That skill does not exist!", new Object[0]);
		} else Messaging.send(sender, "Unrecognized command!", new Object[0], ChatColor.RED);
		
		return true;
	}

}
