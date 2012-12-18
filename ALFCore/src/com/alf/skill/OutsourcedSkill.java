package com.alf.skill;

import org.bukkit.command.CommandSender;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;

public class OutsourcedSkill extends Skill {

	public OutsourcedSkill(AlfCore plugin, String name) {
		super(plugin, name);
	}
	
	public boolean execute(CommandSender cs, String command, String[] args) 
	{	return true;	}

	public void tryLearningSkill(Alf alf) {
		
	}

}
