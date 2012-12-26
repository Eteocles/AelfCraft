package com.alf.skill;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Describes a delayed, targeted type spell.
 * @author Eteocles
 */
public class DelayedTargetedSkill extends DelayedSkill {

	private final LivingEntity target;
	
	/**
	 * Constructs the skill.
	 * @param identifier
	 * @param player
	 * @param warmup
	 * @param skill
	 * @param target
	 * @param args
	 */
	public DelayedTargetedSkill(String identifier, Player player, long warmup, Skill skill,
			LivingEntity target, String[] args) {
		super(identifier, player, warmup, skill, args);
		this.target = target;
	}
	
	/**
	 * Get the target for this skill.
	 * @return
	 */
	public LivingEntity getTarget() {
		return target;
	}

}
