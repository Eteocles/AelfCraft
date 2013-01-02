package com.alf.command;

import org.bukkit.command.CommandSender;

/**
 * A command with finite state automata.
 * @author Eteocles
 */
public interface InteractiveCommand extends Command {

	/**
	 * Get the identifier to cancel the interaction.
	 * @return
	 */
	public abstract String getCancelIdentifier();

	/**
	 * Handle command cancelling.
	 * @param sender
	 */
	public abstract void onCommandCancelled(CommandSender sender);

}
