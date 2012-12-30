package com.alf.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.api.SkillUseInfo;
import com.alf.character.CharacterTemplate;
import com.alf.skill.Skill;

/**
 * Describes an event in which a skill is used to damage an entity.
 * @author Eteocles
 */
public class SkillDamageEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private int damage;
	private final CharacterTemplate damager;
	private final Entity entity;
	private final Skill skill;
	private boolean cancelled = false;

	/**
	 * Construct the event.
	 */
	public SkillDamageEvent(int damage, Entity entity, SkillUseInfo skillInfo) {
		this.damage = damage;
		this.damager = skillInfo.getCharacter();
		this.skill = skillInfo.getSkill();
		this.entity = entity;
	}

	/**
	 * Get the damage dealt.
	 * @return
	 */
	public int getDamage()
	{	return this.damage;	}

	/**
	 * Get the damaging entity.
	 * @return
	 */
	public CharacterTemplate getDamager()
	{	return this.damager;	}

	/**
	 * Get the entity.
	 * @return
	 */
	public Entity getEntity()
	{	return this.entity;	}

	/**
	 * Get the skill.
	 * @return
	 */
	public Skill getSkill()
	{	return this.skill;	}

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
	 * Set the damage dealt.
	 * @param damage
	 */
	public void setDamage(int damage) 
	{	this.damage = damage;	}

	public HandlerList getHandlers()
	{	return handlers;	}

	public static HandlerList getHandlerList() 
	{	return handlers;	}
	
}
