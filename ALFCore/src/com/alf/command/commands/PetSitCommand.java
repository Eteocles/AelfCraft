package com.alf.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.Pet;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

public class PetSitCommand extends BasicCommand {

	private final AlfCore plugin;
	
	public PetSitCommand(AlfCore plugin) {
		super("Pet Sit");
		this.plugin = plugin;
		setDescription("Toggle whether your pet sits or not.");
		setUsage("/pet sit");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "pet sit" });
	}

	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (!(cs instanceof Player))
			return false;
		
		Player player = (Player) cs;
		
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		Pet pet = alf.getPet();

		if (pet != null) {
			int actionState = pet.getAction();
			pet.setAction(actionState == 1 ? 0 : 1);
			Messaging.send(cs, "Your pet is now $1.", new Object[] { (actionState == 1) ? "following you" : "sitting" }, ChatColor.AQUA);
		} else 
			Messaging.send(cs, "You don't have a pet!", new Object[0]);
		return true;
	}
	
}
