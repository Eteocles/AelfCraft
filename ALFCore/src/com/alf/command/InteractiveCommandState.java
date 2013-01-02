package com.alf.command;

import org.bukkit.command.CommandSender;

/**
 * A finite state for the interactive command type.
 * @author Eteocles
 */
public abstract interface InteractiveCommandState {

	/**
	 * Execute the state.
	 * @param sender
	 * @param desc
	 * @param args
	 * @return
	 */
	public abstract boolean execute(CommandSender sender, String desc, String[] args);

	/**
	 * Get the maximum arguments for this state.
	 * @return
	 */
	public abstract int getMaxArguments();

	/**
	 * Get the minimum arguments for this state.
	 * @return
	 */
	public abstract int getMinArguments();

	/**
	 * Whether the parameter is an identifier.
	 * @param paramString
	 * @return
	 */
	public abstract boolean isIdentifier(String paramString);
	
}
