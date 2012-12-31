package com.alf.chat.util;

import org.bukkit.inventory.ItemStack;

public class Mail {
	
	public ItemStack attached;
	public String message;
	
	public Mail(String message, ItemStack attached) {
		this.message = message;
		this.attached = attached;
	}
	
}