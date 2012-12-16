package com.alf.api;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * The cause for an Alf being damaged in a melee event.
 * @author Eteocles
 */
public class AlfAttackDamageCause extends AlfDamageCause {
	
	private ItemStack weapon = null;
	private final Entity attacker;
	
	/**
	 * Construct an AlfAttackDamageCause.
	 * @param damage
	 * @param cause
	 * @param attacker
	 */
	public AlfAttackDamageCause(int damage, EntityDamageEvent.DamageCause cause, Entity attacker) {
		super(damage, cause);
		this.attacker = attacker;
		if (attacker instanceof Player) 
			this.weapon = ((Player)attacker).getItemInHand();
	}
	
	/**
	 * Get the attacking entity.
	 * @return
	 */
	public Entity getAttacker() {
		return this.attacker;
	}
	
	/**
	 * Get the item stack being used as a weapon (shallow copy).
	 * @return
	 */
	public ItemStack getWeapon() {
		return this.weapon == null ? null : this.weapon.clone();
	}
	
}
