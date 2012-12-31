package com.alf.character.effect.common;

import com.alf.character.effect.Effect;
import com.alf.character.effect.EffectType;
import com.alf.skill.Skill;

public class PetEffect extends Effect {

	public PetEffect(Skill skill) {
		super(skill, "Pet");
		this.types.add(EffectType.BENEFICIAL);
	}
}
