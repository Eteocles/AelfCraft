package com.alf.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.chararacter.Alf;
import com.alf.chararacter.CharacterTemplate;
import com.alf.chararacter.effect.CombatEffect;

/**
 * Describes an event in which an Alf kills a character.
 * @author Eteocles
 */
public class AlfKillCharacterEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final CharacterTemplate defender;
	private final Alf attacker;
	private final CombatEffect.CombatReason reason;

	/**
	 * Construct the event.
	 * @param defender
	 * @param attacker
	 */
	public AlfKillCharacterEvent(CharacterTemplate defender, Alf attacker) {
		this.defender = defender;
		this.attacker = attacker;
		this.reason = ((CombatEffect.CombatReason)attacker.getCombatants().get(defender.getEntity()));
	}

	/**
	 * Get the handlers.
	 */
	public HandlerList getHandlers()
	{	return handlers;	}

	public static HandlerList getHandlerList() 
	{	return handlers;	}

	/**
	 * Get the reason for the kill.
	 * @return
	 */
	public CombatEffect.CombatReason getReason()
	{	return this.reason;	}

	/**
	 * Get the attacker.
	 * @return
	 */
	public Alf getAttacker()
	{	return this.attacker;	}

	/**
	 * Get the defending character that was killed.
	 * @return
	 */
	public CharacterTemplate getDefender()
	{	return this.defender;	}
	
}
