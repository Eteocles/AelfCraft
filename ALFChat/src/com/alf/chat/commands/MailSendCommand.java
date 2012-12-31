package com.alf.chat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.alf.chat.AlfChat;
import com.alf.chat.ChatManager;
import com.alf.chat.util.Mail;
import com.alf.command.BasicCommand;
import com.alf.util.Messaging;

public class MailSendCommand extends BasicCommand {

	private final AlfChat plugin;

	public MailSendCommand(AlfChat plugin) {
		super("Mail Send");
		this.plugin = plugin;
		setDescription("Send mail to other players.");
		setUsage("/mail send §8[flag] [player] [message]");
		setArgumentRange(2, Integer.MAX_VALUE);
		setIdentifiers(new String[] { "mail send" });
	}

	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
		if (cs instanceof Player) {
			ChatManager cm = this.plugin.getChatManager();

			//Send mail. /mail send [flags] [user] [message]
			String recipient;
			String message = "";
			ItemStack attachment = null;

			// /mail send -i Eteocles hi ete
			if (args[0].equals("-i"))
				if (args.length > 2) {
					attachment = ((Player)cs).getItemInHand();
				}
				else {
					Messaging.send(cs, "You are missing an argument. The proper format is /mail send §8[flag] [player] [message]",
							new Object[0]);
					return true;
				}
			recipient = (attachment == null) ? args[0] : args[1];
			int pos = (attachment == null) ? 1 : 2;
			for (int i = pos; i < args.length; i++)
				message += args[i] + " ";
			
			if (cm.sendMail(cs, recipient, new Mail(message, attachment))) {
				Messaging.send(cs, "Your mail has been sent!", new Object[0]);
				if (attachment != null)
					((Player)cs).getInventory().setItemInHand(null);
			}
			else
				Messaging.send(cs, "Player could not be found.", new Object[0], ChatColor.RED);
			return true;
		}
		return false;
	}
}
