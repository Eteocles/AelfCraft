package com.alf.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.classes.AlfClass;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;
import com.alf.util.Properties;

public class AdminLevelCommand extends BasicCommand {

	private final AlfCore plugin;

	public AdminLevelCommand(AlfCore plugin)
	{
		super("AdminLevelCommand");
		this.plugin = plugin;
		setDescription("Changes a users level");
		setUsage("/alf admin level §9<player> <class> <level>");
		setArgumentRange(3, 3);
		setIdentifiers(new String[] { "alf admin level" });
		setPermission("alf.admin.level");
	}

	public boolean execute(CommandSender sender, String identifier, String[] args)
	{
		Player player = this.plugin.getServer().getPlayer(args[0]);

		if (player == null) {
			Messaging.send(sender, "Failed to find a matching Player for '$1'.  Offline players are not supported!", new Object[] { args[0] });
			return false;
		}
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		AlfClass hc = this.plugin.getClassManager().getClass(args[1]);

		if (hc == null) {
			if (args[1].equalsIgnoreCase("prim"))
				hc = alf.getAlfClass();
			else if (args[1].equalsIgnoreCase("prof")) {
				hc = alf.getSecondClass();
			}
		}

		if (hc == null) {
			Messaging.send(sender, "$1 is not a valid AlfClass!", new Object[] { args[1] });
			return false;
		}
		try
		{
			int levelChange = Integer.parseInt(args[2]);
			if (levelChange < 1) {
				throw new NumberFormatException();
			}
			int experience = Properties.levels[(levelChange - 1)];
			alf.addExp(experience - alf.getExperience(hc), hc, alf.getPlayer().getLocation());
			this.plugin.getCharacterManager().saveAlf(alf, false);
			Messaging.send(sender, "Level changed.", new Object[0]);
			return true;
		} catch (NumberFormatException e) {
			Messaging.send(sender, "Invalid level value.", new Object[0]);
		}return false;
	}
}
