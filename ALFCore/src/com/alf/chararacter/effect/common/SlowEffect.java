package com.alf.chararacter.effect.common;

import com.alf.chararacter.Alf;
import com.alf.chararacter.effect.ExpirableEffect;
import com.alf.skill.Skill;

public class SlowEffect extends ExpirableEffect {

	public SlowEffect(Skill skill, String name, long duration, int amplifier, boolean swing, 
			String string2, String string3, Alf alf) {
		super(skill, name, duration);
	}

}
