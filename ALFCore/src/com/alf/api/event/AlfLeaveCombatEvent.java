package com.alf.api.event;

import java.util.Collections;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.character.effect.CombatEffect;

/**
 * Describes an event in which an Alf leaves combat.
 * @author Eteocles
 */
public class AlfLeaveCombatEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final CombatEffect.LeaveCombatReason reason;
	private final Alf alf;
	private final Map<LivingEntity, CombatEffect.CombatReason> combatants;

	/**
	 * Construct the event.
	 * @param alf
	 * @param combatants
	 * @param reason
	 */
	public AlfLeaveCombatEvent(Alf alf, Map<LivingEntity, CombatEffect.CombatReason> combatants, CombatEffect.LeaveCombatReason reason) {
		this.alf = alf;
		this.reason = reason;
		this.combatants = combatants;
	}
	
	/**
	 * Get the reason for leaving combat.
	 * @return
	 */
	public CombatEffect.LeaveCombatReason getReason()
	{	return this.reason;	}
	
	/**
	 * Get the Alf leaving combat.
	 * @return
	 */
	public Alf getAlf()
	{	return this.alf;	}
	
	/**
	 * Get the combatants.
	 * @return
	 */
	public Map<LivingEntity, CombatEffect.CombatReason>getCombatants()
	{	return Collections.unmodifiableMap(this.combatants);	}
	
	public HandlerList getHandlers()
	{	return handlers;	}
	
	public static HandlerList getHandlerList()
	{	return handlers;	}

}
