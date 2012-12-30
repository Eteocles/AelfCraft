package com.alf.character.effect;

import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.Monster;
import com.alf.skill.Skill;

public class PeriodicHealEffect extends PeriodicExpirableEffect {

	public PeriodicHealEffect(AlfCore plugin, String name, long period, long duration, 
			int tickHealth, Player applier) {
		super(null, plugin, name, period, duration);
		// TODO Auto-generated constructor stub
	}
	
	public PeriodicHealEffect(Skill skill, String name, long period, long duration, int tickHealth, Player applier) {
		super(skill, name, period, duration);
		
	}

	@Override
	public void tickMonster(Monster m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickAlf(Alf a) {
		// TODO Auto-generated method stub
		
	}

}
