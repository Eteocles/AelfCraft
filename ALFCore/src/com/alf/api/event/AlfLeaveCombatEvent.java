package com.alf.api.event;

import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.chararacter.Alf;
import com.alf.chararacter.effect.CombatEffect.CombatReason;
import com.alf.chararacter.effect.CombatEffect.LeaveCombatReason;

public class AlfLeaveCombatEvent extends Event {

	public AlfLeaveCombatEvent(Alf alf,
			Map<LivingEntity, CombatReason> combatMap, LeaveCombatReason timed) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return null;
	}

}
