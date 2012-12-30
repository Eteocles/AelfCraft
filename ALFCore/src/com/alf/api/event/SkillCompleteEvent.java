package com.alf.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.skill.Skill;

/**
 * Describes an event in which a skill is completed.
 * @author Eteocles
 */
public class SkillCompleteEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Alf alf;
	private final Skill skill;
	private final SkillResult result;

	/**
	 * Construct the event.
	 * @param alf
	 * @param skill
	 * @param result
	 */
	public SkillCompleteEvent(Alf alf, Skill skill, SkillResult result) {
		this.alf = alf;
		this.skill = skill;
		this.result = result;
	}

	/**
	 * Get the Alf completing the skill.
	 * @return
	 */
	public Alf getAlf()
	{	return this.alf;	}

	/**
	 * Get the skill used.
	 * @return
	 */
	public Skill getSkill()
	{	return this.skill;	}

	/**
	 * Get the result of the skill.
	 * @return
	 */
	public SkillResult getResult()
	{	return this.result;	}

	public HandlerList getHandlers()
	{	return handlers;	}

	public static HandlerList getHandlerList() 
	{	return handlers;	}
}
