package com.alf.api;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

import com.alf.skill.Skill;

/**
 * The cause behind an Alf being damaged by a skill.
 * @author Eteocles
 * 
 */
public class AlfSkillDamageCause extends AlfDamageCause {
	
	private final Entity attacker;
	private final Skill skill;
	
	/**
	 * Construct an AlfSkillDamageCause.
	 * @param damage
	 * @param cause
	 * @param attacker
	 * @param skill
	 */
	public AlfSkillDamageCause(int damage, EntityDamageEvent.DamageCause cause, Entity attacker, Skill skill) {
		super(damage, cause);
		this.attacker = attacker;
		this.skill = skill;
	}
	
	/**
	 * Get the attacking entity.
	 * @return
	 */
	public Entity getAttacker() {
		return this.attacker;
	}
	
	/**
	 * Get the skill being used.
	 * @return
	 */
	public Skill getSkill() {
		return this.skill;
	}
}
