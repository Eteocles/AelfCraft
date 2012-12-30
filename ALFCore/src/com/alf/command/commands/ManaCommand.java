package com.alf.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

/**
 * A command that displays a player's mana.
 * @author Eteocles
 */
public class ManaCommand extends BasicCommand {

	private final AlfCore plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public ManaCommand(AlfCore plugin) {
		super("Mana");
		this.plugin = plugin;
		setDescription("Displays your current mana");
		setUsage("/mana");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "mana", "aelf mana", "alf mana" });
	}

	/**
	 * Execute the command.
	 */
	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		
		if (! (cs instanceof Player))
			return false;
		
		Player player = (Player) cs;
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		
		int mana = alf.getMana();
		int maxMana = alf.getMaxMana();
		player.sendMessage("§9Mana: §f" + mana + "/" + maxMana + " " + Messaging.createManaBar(mana, maxMana));
		return true;
	}

}
