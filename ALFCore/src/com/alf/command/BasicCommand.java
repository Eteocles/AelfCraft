package com.alf.command;

import org.bukkit.command.CommandSender;

/**
 * Abstract class type describing a simple type of command.
 * @author Eteocles
 */
public abstract class BasicCommand implements Command {

	//Instance variables.
	private String name;
	private String description = "";
	private String usage = "";
	private String permission = "";
	private String[] notes = new String[0];
	private String[] identifiers = new String[0];
	private int minArguments = 0;
	private int maxArguments = 0;
	
	/** Construct a Basic Command */
	public BasicCommand(String name) {
		this.name = name;
	}
	
	/** BasicCommands are not interactive, therefore this method does nothing. */
	public void cancelInteraction(CommandSender executor) {}
	
	/**
	 * Get the description for this basic command. 
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Get the identifiers (aliases) for the command.
	 */
	public String[] getIdentifiers() {
		return this.identifiers;
	}
	
	/**
	 * Get the maximum number of arguments permitted for the command.
	 */
	public int getMaxArguments() {
		return this.maxArguments;
	}
	
	/**
	 * Get the minimum number of arguments permitted for the command.
	 */
	public int getMinArguments() {
		return this.minArguments;
	}
	
	/**
	 * Get the name of this command.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get the notes for this command.
	 */
	public String[] getNotes() {
		return this.notes;
	}
	
	/**
	 * Get the Permission node for this command.
	 */
	public String getPermission() {
		return this.permission;
	}
	
	/**
	 * Get the usage for this command.
	 */
	public String getUsage() {
		return this.usage;
	}
	
	/**
	 * Whether an input string is an identifier for this command.
	 */
	public boolean isIdentifier(CommandSender executor, String input) {
		for (String id : this.identifiers)
			if (input.equalsIgnoreCase(id))
				return true;
		return false;
	}
	
	/**
	 * The BasicCommand is not interactive and therefore has only one state.
	 */
	public boolean isInProgress(CommandSender executor) {
		return false;
	}
	
	/**
	 * Whether the BasicCommand is interactive.
	 */
	public boolean isInteractive() {
		return false;
	}
	
	/**
	 * Whether the BasicCommand is shown on the help menu.
	 */
	public boolean isShownOnHelpMenu() {
		return true;
	}
	
	/**
	 * Set the argument range for the command.
	 * @param min - minimum number of arguments
	 * @param max - maximum number of arguments
	 */
	public void setArgumentRange(int min, int max) {
		this.minArguments = min;
		this.maxArguments = max;
	}
	
	/**
	 * Set the description for the command.
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Set the identifiers for the command.
	 * @param identifiers
	 */
	public void setIdentifiers(String[] identifiers) {
		this.identifiers = identifiers;
	}
	
	/**
	 * Set the notes for the command.
	 * @param notes
	 */
	public void setNote(String[] notes) {
		this.notes = notes;
	}
	
	/**
	 * Set the permission node for the command.
	 * @param permission
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	/**
	 * Set the usage for the command.
	 * @param usage
	 */
	public void setUsage(String usage) {
		this.usage = usage;
	}
}
