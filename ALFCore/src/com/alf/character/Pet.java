package com.alf.character;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.alf.AlfCore;

/**
 * The pet character type is invulnerable but can not deal damage.
 * The pet is mostly cosmetic, but is adorable!
 * Plus, it delivers your mail.
 * @author Eteocles
 */
public class Pet extends Monster {

	private Alf owner;
	
	/**
	 * Construct the pet.
	 * @param plugin
	 * @param lEntity
	 * @param name
	 * @param owner
	 */
	public Pet(AlfCore plugin, LivingEntity lEntity, String name, Alf owner) {
		super(plugin, lEntity, name);
		this.owner = owner;
		setMaxHealth(plugin.getCharacterManager().getMaxHealth(lEntity));
		lEntity.setHealth(lEntity.getMaxHealth());
	}
	
	/**
	 * Custom spawn reason.
	 * @return
	 */
	public CreatureSpawnEvent.SpawnReason getSpawnReason() {
		return CreatureSpawnEvent.SpawnReason.CUSTOM;
	}
	
	/**
	 * Get the pet's owner.
	 * @return
	 */
	public Alf getOwner() {
		return this.owner;
	}
	
	public void setOwner(Alf alf) {
		this.owner = alf;
	}
	
}
