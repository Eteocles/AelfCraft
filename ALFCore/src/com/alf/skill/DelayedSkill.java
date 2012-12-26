package com.alf.skill;

import org.bukkit.entity.Player;

/**
 * Contains a skill that runs on a delayed basis.
 * @author Eteocles
 */
public class DelayedSkill {

	private final String identifier;
	private final String[] args;
	private final long startTime;
	private final long warmup;
	private final Skill skill;
	private final Player player;
	
	/**
	 * Construct a delayed skill.
	 * @param identifier
	 * @param player
	 * @param warmup
	 * @param skill
	 * @param args
	 */
	public DelayedSkill(String identifier, Player player, long warmup,
			Skill skill, String[] args) {
		this.identifier = identifier;
		this.player = player;
		this.warmup = warmup;
		this.skill = skill;
		this.args = args;
		this.startTime = System.currentTimeMillis();
	}

	/**
	 * Whether the delayed skill is ready.
	 * @return
	 */
	public boolean isReady() {	
		return System.currentTimeMillis() >= this.startTime + this.warmup;	
	}
	
	/**
	 * Get the start time.
	 * @return
	 */
	public long startTime() {	
		return this.startTime;	
	}
	
	/**
	 * Get the identifier for this skill.
	 * @return
	 */
	public String getIdentifier() {	
		return this.identifier;	
	}
	
	/**
	 * Get the arguments for this skill.
	 * @return
	 */
	public String[] getArgs() {	
		return this.args;	
	}
	
	/**
	 * Get the encapsulated skill.
	 * @return
	 */
	public Skill getSkill() {	
		return this.skill;	
	}
	
	/**
	 * Get the player using the skill.
	 * @return
	 */
	public Player getPlayer() {	
		return this.player;	
	}

}
