package com.alf.chararacter.effect;

import org.bukkit.entity.LivingEntity;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;
import com.alf.chararacter.effect.CombatEffect.CombatReason;
import com.alf.chararacter.effect.CombatEffect.LeaveCombatReason;

public class CombatEffect extends PeriodicEffect {

	public static enum LeaveCombatReason {
		DEATH,
		SUICIDE,
		ERROR,
		TIMED,
		LOGOUT,
		TARGET_DEATH,
		TARGET_LOGOUT,
		CUSTOM;
	}
	
	public static enum CombatReason {
		DAMAGED_BY_MOB,
		DAMAGED_BY_PLAYER,
		ATTACKED_MOB,
		ATTACKED_PLAYER,
		CUSTOM;
	}

	public CombatEffect(AlfCore plugin) {
		
	}

	public boolean isInCombat() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isInCombatWith(LivingEntity target) {
		// TODO Auto-generated method stub
		return false;
	}

	public void enterCombatWith(LivingEntity entity, CombatReason reason) {
		// TODO Auto-generated method stub
		
	}

	public void reset() {
		// TODO Auto-generated method stub
		
	}

	public void leaveCombatWith(Alf alf, LivingEntity entity,
			LeaveCombatReason reason) {
		// TODO Auto-generated method stub
		
	}
	
}
