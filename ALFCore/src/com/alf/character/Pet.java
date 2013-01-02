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
	//Moving, Stationary, ??
	private int actionId;
	
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
		actionId = 0;
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

	/**
	 * Get the action identifier for this pet.
	 * @return - the determinant for behavior
	 */
	public int getAction() {
		return this.actionId;
	}
	
	public void toggleStationary() {
		this.actionId = (this.actionId == 0) ? 1 : 0;
	}
	
	/**
	 * Set how this pet will behave.
	 * @param type
	 */
	public void setAction(int type) {
		this.actionId = type;
	}
	
}
