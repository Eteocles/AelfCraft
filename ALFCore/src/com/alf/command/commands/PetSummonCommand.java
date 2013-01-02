package com.alf.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.Pet;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

public class PetSummonCommand extends BasicCommand {

	private final AlfCore plugin;

	public PetSummonCommand(AlfCore plugin) {
		super("Pet Summon");
		this.plugin = plugin;
		setDescription("Summons your pet.");
		setUsage("/pet summon");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "pet summon", "pet call" });
	}

	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (!(cs instanceof Player))
			return false;
		
		Player player = (Player) cs;
		
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		Pet pet = alf.getPet();

		if (pet != null) {
			//Teleport to player.
			pet.getEntity().teleport(player);
			if (player.hasPermission("alf.pet.lightning"))
					player.getWorld().strikeLightningEffect(pet.getEntity().getLocation());
			Messaging.send(cs, "You summon your pet in all of its glory.", new Object[0], ChatColor.AQUA);
		} else 
			Messaging.send(cs, "You don't have a pet! Why would you ever think of killing it if you had one?", new Object[0]);
		return true;
	}
}
