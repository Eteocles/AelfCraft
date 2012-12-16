package com.alf.chararacter;

import java.util.*;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.alf.AlfCore;
import com.alf.chararacter.effect.Effect;

/**
 * Describes a Monster type object.
 * @author Eteocles
 */
public class Monster extends CharacterTemplate {

	private int maxHealth = 0;
	private int damage = 0;
	private int experience = -1;
	private CreatureSpawnEvent.SpawnReason spawnReason = null;
	//A monster can periodically use a certain skill.
	private Map<String, ConfigurationSection> skills = new HashMap<String, ConfigurationSection>();

	public Monster(AlfCore plugin, LivingEntity lEntity) { this(plugin, lEntity, null); }

	/**
	 * Construct a Monster.
	 * @param plugin
	 * @param lEntity
	 * @param name
	 */
	public Monster(AlfCore plugin, LivingEntity lEntity, String name) {
		super(plugin, lEntity, name);
		int damage = plugin.getDamageManager().getEntityDamage(lEntity.getType());
		this.damage = damage;
		if (lEntity instanceof Slime) {
			Slime slime = (Slime) lEntity;
			switch (slime.getSize()) {
				case 1:
					if (slime instanceof MagmaCube)
						this.damage -= damage / 3;
					else this.damage = 0;
					break;
				case 2: this.damage -= damage / 3;
			}
		}
		setMaxHealth(plugin.getCharacterManager().getMaxHealth(lEntity));
		lEntity.setHealth(lEntity.getMaxHealth());
	}

	/**
	 * Set the maximum health for this monster.
	 * @param maxHealth
	 * @return
	 */
	public boolean setMaxHealth(int maxHealth) {
		int health = this.health.get();
		if (health != this.maxHealth || maxHealth <= 0)
			return false;
		this.maxHealth = maxHealth;
		this.health.getAndSet(maxHealth);
		return true;
	}
	
	/**
	 * Get the maximum health for this monster.
	 */
	public int getMaxHealth() {
		return this.maxHealth;
	}
	
	/**
	 * Get the amount of damage dealt by this monster.
	 * @return
	 */
	public int getDamage() {
		return this.damage;
	}

	/**
	 * Set the amount of damage dealt by this monster.
	 * @param damage
	 */
	public void setDamage(int damage) {
		this.damage = damage;
	}
	
	/**
	 * Clear all of this monster's effects.
	 */
	public void clearEffects() {
		for (Effect effect : getEffects())
			removeEffect(effect);
	}
	
	/**
	 * Whether this Monster is equal to the object.
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof LivingEntity) 
			return this.lEntity.equals(obj);
		if (obj instanceof UUID)
			return this.lEntity.getUniqueId().equals(obj);
		if (obj instanceof Monster)
			return this.lEntity.getUniqueId().equals(((Monster)obj).lEntity.getUniqueId());
		return false;
	}
	
	/**
	 * Set the experience for this monster.
	 * @param experience
	 */
	public void setExperience(int experience) {
		this.experience = experience;
	}

	/**
	 * Get the experience yielded from this monster.
	 * @return
	 */
	public int getExperience() {
		return this.experience;
	}
	
	/**
	 * Set the reason for this monster spawn.
	 * @param reason
	 */
	public void setSpawnReason(CreatureSpawnEvent.SpawnReason reason) {
		this.spawnReason = reason;
	}
	
	/**
	 * Get the reason for this monster spawning.
	 * @return
	 */
	public CreatureSpawnEvent.SpawnReason getSpawnReason() {
		return this.spawnReason;
	}
}