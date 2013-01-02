package com.alf.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.Pet;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

public class PetKillCommand extends BasicCommand {

	private final AlfCore plugin;

	public PetKillCommand(AlfCore plugin) {
		super("Pet Kill");
		this.plugin = plugin;
		setDescription("Kills your pet. Jerk.");
		setUsage("/pet kill");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "pet kill" });
	}

	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (! (cs instanceof Player))
			return false;
		
		Alf alf = this.plugin.getCharacterManager().getAlf((Player)cs);
		Pet pet = alf.getPet();

		if (pet != null) {
			LivingEntity entity = pet.getEntity();
			
			if (((Player)cs).hasPermission("alf.pet.lightning"))
					entity.getWorld().strikeLightningEffect(entity.getLocation());
			
			//Remove the pet from the manager and despawn it.
			this.plugin.getCharacterManager().removePet(pet);
			alf.removeEffect(alf.getEffect("Follow"));
			alf.setPet(null);

			Messaging.send(cs, "You killed your pet. How could you?", new Object[0]);
		} else 
			Messaging.send(cs, "You don't have a pet! Why would you ever think of killing it if you had one?", new Object[0]);
		return true;
	}

}
