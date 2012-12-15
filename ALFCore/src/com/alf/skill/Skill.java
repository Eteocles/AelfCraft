package com.alf.skill;

import com.alf.AlfCore;
import com.alf.command.BasicCommand;

public abstract class Skill extends BasicCommand {

	public final AlfCore plugin;
	
	public Skill(AlfCore plugin, String name) {
		super(name);
		this.plugin = plugin;
	}
	
}
