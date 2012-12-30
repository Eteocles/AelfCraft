package com.alf.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Describes an event in which a character is damaged.
 * @author Eteocles
 */
public class CharacterDamageEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final Entity entity;
	private int damage;
	private final EntityDamageEvent.DamageCause cause;

	/**
	 * Construct the event.
	 * @param entity
	 * @param cause
	 * @param damage
	 */
	public CharacterDamageEvent(Entity entity, EntityDamageEvent.DamageCause cause, int damage) {
		this.entity = entity;
		this.cause = cause;
		this.damage = damage;
	}

	/**
	 * Get amount of damage dealt.
	 * @return
	 */
	public int getDamage()
	{	return this.damage;	}

	/**
	 * Set the amount of damage dealt.
	 * @param damage
	 */
	public void setDamage(int damage)
	{	this.damage = damage;	}

	/**
	 * get the entity being damaged.
	 * @return
	 */
	public Entity getEntity()
	{	return this.entity;	}

	/**
	 * Get the cause of damage.
	 * @return
	 */
	public EntityDamageEvent.DamageCause getCause()
	{	return this.cause;	}

	public HandlerList getHandlers()
	{	return handlers;	}

	public static HandlerList getHandlerList() 
	{	return handlers;	}

	public boolean isCancelled()
	{	return this.cancelled;	}

	public void setCancelled(boolean val) 
	{	this.cancelled = val;	}
}
