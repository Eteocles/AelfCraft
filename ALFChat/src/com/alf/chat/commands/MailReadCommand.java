package com.alf.chat.commands;


import org.bukkit.command.CommandSender;

import com.alf.chat.AlfChat;
import com.alf.command.BasicCommand;

public class MailReadCommand extends BasicCommand {

//	private final AlfChat plugin;
//	private static final int MAIL_PER_PAGE = 8;

	public MailReadCommand(AlfChat plugin) {
		super("Mail Read");
//		this.plugin = plugin;
		setDescription("Read your mail.");
		setUsage("/mail read <args>");
		setArgumentRange(0, 1);
		setIdentifiers(new String[] { " mail read" });
	}
	
	@Override
	public boolean execute(CommandSender cs, String msg, String[] args) {
//		if (cs instanceof Player) {
//			ChatManager cm = this.plugin.getChatManager();
//			CharacterManager chm = AlfChat.corePlugin.getCharacterManager();
//			Alf alf = chm.getAlf((Player) cs);
//			Pet pet = alf.getPet();
//			ChPlayer cplayer = cm.getChPlayer((Player)cs);
//			Map<String, List<Mail>> unreadMail = cplayer.getUnreadMail();
//			Map<String, List<Mail>> readMail = cplayer.getReadMail();
			
//			if (pet == null) {
//				//Output mail normally.
//			} else {
//				//Write pet specific mail.
//			}
//			
//			if (args.length == 0) {
//				//Show first page of new mail by default.
//				
//			}
			
			
//		} else {
//			Messaging.send(cs, "You can't have mail as Console.", new Object[0]);
//		}
		return true;
	}
	
//	public Map<Integer, List<String>> getDisplayMail(Map<String, List<Mail>> mail) {
//		Map<Integer, List<String>> mailPages = new HashMap<Integer, List<String>>();
//		int totalMail = 0;
//		for (String sender : mail.keySet())
//			totalMail += mail.get(sender).size();
//		int numPages = totalMail / MAIL_PER_PAGE;
//		if (totalMail % MAIL_PER_PAGE != 0)
//			numPages++;
//		
//		int modCount = 0;
//		int page = 0;
//		
//		
//		return mailPages;
//	}

}
