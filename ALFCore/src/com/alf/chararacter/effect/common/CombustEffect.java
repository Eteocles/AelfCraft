package com.alf.chararacter.effect.common;

import org.bukkit.entity.Player;

import com.alf.chararacter.Alf;
import com.alf.chararacter.Monster;
import com.alf.chararacter.effect.PeriodicExpirableEffect;
import com.alf.skill.Skill;

public class CombustEffect extends PeriodicExpirableEffect {

	public CombustEffect(Skill skill, Player applier) {
		super(skill, "Combust", 10L, 0L);
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
