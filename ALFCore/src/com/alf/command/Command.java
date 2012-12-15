package com.alf.command;

import org.bukkit.command.CommandSender;

/**
 * Abstract interface for declaring Command types.
 * @author Eteocles
 */
public abstract interface Command {

	/** Cancel interaction with an interactive command. Doesn't do anything for non-interactive types. */
	public abstract void cancelInteraction(CommandSender cs);
	
	/** Execute the command with the given Command Sender, command string, and arguments. */
	public abstract boolean execute(CommandSender cs, String msg, String[] args);
	
	/** Get the description for the command. */
	public abstract String getDescription();

	/** Get the identifiers (aliases) for the command. */
	public abstract String[] getIdentifiers();
	
	/** Get the maximum number of arguments permitted for the command. */
	public abstract int getMaxArguments();

	/** Get the minimum number of arguments permitted for the command. */
	public abstract int getMinArguments();
	
	/** Get the name of the command. */
	public abstract String getName();
	
	/** Get any comments or notes attached to this command. */
	public abstract String[] getNotes();
	
	/** Get the String permission for the command. */
	public abstract String getPermission();
	
	/** Get the usage for the command. */
	public abstract String getUsage();
	
	/** Whether the given String is an identifier. */
	public abstract boolean isIdentifier(CommandSender cs, String s);
	
	/** Whether the given command sender is in the process of using the command. */
	public abstract boolean isInProgress(CommandSender cs);
	
	/** Whether the command is interactive. */
	public abstract boolean isInteractive();
	
	/** Whether the command is shown on the help menu. */
	public abstract boolean isShownOnHelpMenu();
	
}
