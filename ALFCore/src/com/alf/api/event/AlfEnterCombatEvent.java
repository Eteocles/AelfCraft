package com.alf.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.character.effect.CombatEffect;

/**
 * Describes an event in which an Alf enters combat.
 * @author Eteocles
 */
public class AlfEnterCombatEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final CombatEffect.CombatReason reason;
	private final Alf alf;
	private final LivingEntity target;

	/**
	 * Construct the event.
	 * @param alf
	 * @param target
	 * @param reason
	 */
	public AlfEnterCombatEvent(Alf alf, LivingEntity target, CombatEffect.CombatReason reason)
	{
		this.alf = alf;
		this.reason = reason;
		this.target = target;
	}

	/**
	 * Get the reason for combat.
	 * @return
	 */
	public CombatEffect.CombatReason getReason()
	{	return this.reason;		}

	/**
	 * Get the alf entering combat.
	 * @return
	 */
	public Alf getAlf()
	{	return this.alf;	}

	/**
	 * Get the target.
	 * @return
	 */
	public LivingEntity getTarget()
	{	return this.target;	}

	/**
	 * Get the handlers.
	 */
	public HandlerList getHandlers()
	{	return handlers;	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
