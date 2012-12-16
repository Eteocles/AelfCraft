package com.alf.api;

import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Describes a damage cause for an Alf. 
 * @author Eteocles
 */
public class AlfDamageCause {

	private final EntityDamageEvent.DamageCause cause;
	private final int damage;
	
	/**
	 * Construct an AlfDamageCause.
	 * @param damage
	 * @param cause
	 */
	public AlfDamageCause(int damage, EntityDamageEvent.DamageCause cause) {
		this.damage = damage;
		this.cause = cause;
	}
	
	/**
	 * Get the cause for the Alf's damage.
	 * @return
	 */
	public EntityDamageEvent.DamageCause getCause() {
		return this.cause;
	}
	
	/**
	 * Get the amount of damage dealt.
	 * @return
	 */
	public int getDamage() {
		return this.damage;
	}
	
}
