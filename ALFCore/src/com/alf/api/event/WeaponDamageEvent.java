package com.alf.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.alf.character.CharacterTemplate;

/**
 * Describes an event in which a weapon is used to damage an entity.
 * @author Eteocles
 */
public class WeaponDamageEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private int damage;
	private final CharacterTemplate damager;
	private final Entity entity;
	private final EntityDamageEvent.DamageCause cause;
	private final boolean projectile;
	private boolean cancelled = false;

	/**
	 * Construct the event.
	 * @param damage
	 * @param event
	 * @param damager
	 */
	public WeaponDamageEvent(int damage, EntityDamageByEntityEvent event,
			CharacterTemplate damager) {
		this.damage = damage;
		this.damager = damager;
		this.entity = event.getEntity();
		this.cause = event.getCause();
		this.projectile = (event.getDamager() instanceof Projectile);
	}

	/**
	 * Get the cause for the damage.
	 * @return
	 */
	public EntityDamageEvent.DamageCause getCause()
	{	return this.cause;	}

	/**
	 * Get the damage.
	 * @return
	 */
	public int getDamage()
	{	return this.damage;	}

	/**
	 * Get the damager.
	 * @return
	 */
	public CharacterTemplate getDamager()
	{	return this.damager;	}

	/**
	 * Get the entity being damaged.
	 * @return
	 */
	public Entity getEntity()
	{	return this.entity;	}

	/**
	 * Whether the weapon used was a projectile.
	 * @return
	 */
	public boolean isProjectile()
	{	return this.projectile;	}

	/**
	 * Whether the event is cancelled.
	 */
	public boolean isCancelled()
	{	return this.cancelled;	}

	/**
	 * Set whether the event is cancelled.
	 */
	public void setCancelled(boolean val)
	{	this.cancelled = val;	}

	/**
	 * Set the amount of damage.
	 * @param damage
	 */
	public void setDamage(int damage)
	{	this.damage = damage;	}

	public HandlerList getHandlers()
	{	return handlers;	}

	public static HandlerList getHandlerList() 
	{	return handlers;	}
}
