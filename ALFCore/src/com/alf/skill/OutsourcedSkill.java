package com.alf.skill;

import org.bukkit.command.CommandSender;

import com.alf.AlfCore;

public class OutsourcedSkill extends Skill {

	public OutsourcedSkill(AlfCore plugin, String name) {
		super(plugin, name);
	}
	
	public boolean execute(CommandSender cs, String command, String[] args) 
	{	return true;	}

}
