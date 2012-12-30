package com.alf.character;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.LivingEntity;

import com.alf.AlfCore;
import com.alf.character.effect.CombatEffect;
import com.alf.character.effect.Effect;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.Expirable;
import com.alf.character.effect.Periodic;

/**
 * Encapsulates a living entity and appends additional characteristics.
 * @author Eteocles
 */
public abstract class CharacterTemplate {
	
	private Map<String, Effect> effects = new HashMap<String, Effect>();
	protected final AlfCore plugin;
	protected final LivingEntity lEntity;
	protected AtomicInteger health = new AtomicInteger(0);
	protected final String name;
	
	/**
	 * Constructs a Character.
	 * @param plugin
	 * @param lEntity
	 * @param name
	 */
	public CharacterTemplate(AlfCore plugin, LivingEntity lEntity, String name) {
		this.plugin = plugin;
		this.lEntity = lEntity;
		this.name = name;
	}
	
	/**
	 * Get the name for this character.
	 * @return - the Player's name if a player is encapsulated, a Boss's name, or null
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get an effect applied to this character.
	 * @param name - name of the effect to search
	 * @return - the effect applied or null
	 */
	public Effect getEffect(String name) {
		return (Effect) this.effects.get(name.toLowerCase());
	}
	
	/**
	 * Get the total set of effects for this character.
	 * @return
	 */
	public Set<Effect> getEffects() {
		return new HashSet<Effect>(this.effects.values());
	}
	
	/**
	 * Add an effect to this character.
	 * @param effect
	 */
	public void addEffect(Effect effect) {
		//Remove the effect if it already exists.
		if (hasEffect(effect.getName()))
			removeEffect(getEffect(effect.getName()));
		
		//Manage the effect if it is periodic/expirable.
		if (effect instanceof Periodic || effect instanceof Expirable)
			this.plugin.getEffectManager().manageEffect(this, effect);
		
		//Store the effect.
		this.effects.put(effect.getName().toLowerCase(), effect);
	}
	
	/**
	 * Whether or not this character has an effect.
	 * @param name
	 * @return
	 */
	public boolean hasEffect(String name) {
		return this.effects.containsKey(name.toLowerCase());
	}
	
	/**
	 * Whether the character has an effect type.
	 * @param type
	 * @return
	 */
	public boolean hasEffectType(EffectType type)  {
		for (Effect e : this.effects.values())
			if (e.isType(type))
				return true;
		return false;
	}
	
	/**
	 * Remove an effect from this character.
	 * @param effect
	 */
	public void removeEffect(Effect effect) {
		if (effect != null) {
			if (effect instanceof Expirable || effect instanceof Periodic)
				this.plugin.getEffectManager().queueForRemoval(this, effect);
			effect.remove(this);
			this.effects.remove(effect.getName().toLowerCase());
		}
	}
	
	/**
	 * Manually remove an effect from this character.
	 * @param effect
	 */
	public void manualRemoveEffect(Effect effect) {
		if (effect != null) {
			if (effect instanceof Expirable || effect instanceof Periodic)
				this.plugin.getEffectManager().queueForRemoval(this, effect);
			this.effects.remove(effect.getName().toLowerCase());
		}
	}
	
	/**
	 * Remove all effects for this character.
	 */
	public void clearEffects() {
		for (Effect e : getEffects())
			if (! (e instanceof CombatEffect))
				removeEffect(e);
	}
	
	/**
	 * Get the living entity bound to this character.
	 * @return
	 */
	public LivingEntity getEntity() {
		return this.lEntity;
	}
	
	/**
	 * Get the health of this character.
	 * @return
	 */
	public int getHealth() {
		return this.health.get();
	}
	
	/**
	 * Set the health of this character.
	 * @param health - new health value
	 */
	public void setHealth(int health) {
		int maxHealth = getMaxHealth();
		if (health > maxHealth) 
			health = maxHealth;
		else if (health < 0)
			health = 0;
		this.health.getAndSet(health);
	}
	
	/**
	 * Get the max health of this character.
	 * @return - maximum health value
	 */
	public abstract int getMaxHealth();
	
	/**
	 * Hash code for this object.
	 */
	public int hashCode() {
		return this.lEntity.getUniqueId().hashCode();
	}
	
	/**
	 * Define equality for the character.
	 */
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof CharacterTemplate)
			return ((CharacterTemplate)obj).lEntity.getUniqueId().equals(this.lEntity.getUniqueId());
		return false;
	}
}