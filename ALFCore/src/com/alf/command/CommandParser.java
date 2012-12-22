package com.alf.command;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface CommandParser {

	public void addCommand(Command command);
	
	public Command getCmdFromIdent(String ident, CommandSender executor);
	
	public Command getCommand(String name);
	
	public List<Command> getCommands();
	
	public void removeCommand(Command command);
	
	public boolean dispatch(CommandSender sender, String commandName, String label, String[] args);
	
}
