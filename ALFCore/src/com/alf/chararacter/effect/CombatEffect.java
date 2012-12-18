package com.alf.chararacter.effect;

import java.util.Map;

import org.bukkit.entity.LivingEntity;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;

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
		throw new Error("Implement me!");
	}

	public boolean isInCombatWith(LivingEntity target) {
		throw new Error("Implement me!");
	}

	public void enterCombatWith(LivingEntity entity, CombatReason reason) {
		throw new Error("Implement me!");
	}

	public void reset() {
		throw new Error("Implement me!");
	}

	public void leaveCombatWith(Alf alf, LivingEntity entity,
			LeaveCombatReason reason) {
		throw new Error("Implement me!");
	}

	public void leaveCombatFromLogout(Alf alf) {
		throw new Error("Implement me!");
	}

	public void leaveCombatFromDeath(Alf alf) {
		throw new Error("Implement me!");
	}

	public void leaveCombatFromSuicide(Alf alf) {
		throw new Error("Implement me!");
	}

	public Map<LivingEntity, CombatReason> getCombatants() {
		throw new Error("Implement me!");
	}
	
}
