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
public class EnergyCommand extends BasicCommand {

	private final AlfCore plugin;
	
	/**
	 * Construct the command.
	 * @param plugin
	 */
	public EnergyCommand(AlfCore plugin) {
		super("Energy");
		this.plugin = plugin;
		setDescription("Displays your current energy.");
		setUsage("/energy");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "mana", "energy", "alf energy", "alf mana" });
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
		player.sendMessage("§9Energy: §f" + mana + "/" + maxMana + " " + Messaging.createManaBar(mana, maxMana));
		return true;
	}

}
