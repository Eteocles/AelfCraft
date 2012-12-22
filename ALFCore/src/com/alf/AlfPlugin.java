package com.alf;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.alf.command.CommandParser;
import com.alf.util.DebugLog;

/**
 * Describes general AlfPlugin type.
 * All hooked AelfCraft plugins should extend this class.
 * @author Eteocles
 */
public abstract class AlfPlugin extends JavaPlugin {
	
	private String name;
	//Data folder for the plugin jar.
	private static final File dataFolder = new File("plugins" + File.separator + "AelfCraft");
	//Debug timer for recording output of concurrent tasks.
	public static final DebugTimer debug = new DebugTimer();
	//Logger for plugin output.
	private static Logger log;
	//Debug output.
	private static DebugLog debugLog;
	//Command handler for parsing commands.
	protected CommandParser commandParser;
	
	/**
	 * Constructs the plugin with a name.
	 * @param name - label for the plugin
	 */
	public AlfPlugin(String name) {
		this.name = name;
	}
	
	/**
	 * Passes a message to the Debug Log to be logged for output.
	 * @param level - level of message's severity
	 * @param msg - message to be logged
	 */
	public static void debugLog(Level level, String msg) {
		debugLog.log(level, msg);
	}
	
	/**
	 * Throws a debug output type.
	 * @param sourceClass - class from which throwable originates
	 * @param sourceMethod - method from which throwable originates
	 * @param thrown - throwable object
	 */
	public static void debugThrow(String sourceClass, String sourceMethod, Throwable thrown) {
		debugLog.throwing(sourceClass, sourceMethod, thrown);
	}
	
	/**
	 * Get the command handler for this Alf plugin.
	 * @return - CommandHandler object
	 */
	public CommandParser getCommandParser() {
		return this.commandParser;
	}

	/**
	 * Dispatch the command handling to the handler. 
	 * @return - the results of the dispatch
	 */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return this.commandParser.dispatch(sender, command.getName(), label, args);
	}
	
	/**
	 * Called when Bukkit disables this plugin.
	 */
	public void onDisable() {
		log.info(" version " + getDescription().getVersion() + " is disabled!");
		debugLog.close();
	}
	
	/**
	 * Called when Bukkit enables this plugin.
	 */
	public void onEnable() {
		log = getLogger();
		debug.reset();
		//Override this method in subclasses.
		this.setup();
		
		registerEvents();
		registerCommands();
		
		//Setup for default configuration file
		getConfig().options().header("Config File \n Please Specify database name and credentials below.");
		getConfig().addDefault("mysql_url", "jdbc:mysql://localhost/ALF");
		getConfig().addDefault("mysql_user", "root");
		getConfig().addDefault("mysql_pass", "pass");
		
		
		log(Level.INFO, "version " + getDescription().getVersion() + " is enabled!");
		this.postSetup();
		
	}
	
	/** Plugin enabling handling before log output. */
	public abstract void setup();
	
	/** Plugin enabling handling after log output. */
	public abstract void postSetup();
	
	/** Handles plugin loading. */
	public void onLoad() {
		dataFolder.mkdirs();
		debugLog = new DebugLog(this.name, dataFolder + File.separator + "debug.log");
	}
	
	/** Register commands for the plugin. */
	protected abstract void registerCommands();
	
	/** Register events for the plugin. */
	protected abstract void registerEvents();
	
	/**
	 * Log messages.
	 * @param level - level of message's severity
	 * @param msg - message to be logged
	 */
	public static void log(Level level, String msg) {
		log.log(level, msg);
		debugLog.log(level, "[ALF] " + msg);
	}
}
