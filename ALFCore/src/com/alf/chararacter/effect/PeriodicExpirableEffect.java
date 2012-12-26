package com.alf.chararacter.effect;

import com.alf.AlfCore;
import com.alf.chararacter.Alf;
import com.alf.chararacter.CharacterTemplate;
import com.alf.chararacter.Monster;
import com.alf.skill.Skill;

/**
 * Describes a condition both periodic and expirable.
 * @author Eteocles
 */
public abstract class PeriodicExpirableEffect extends ExpirableEffect implements Periodic {
	
	private final long period;
	protected long lastTickTime = 0L;
	
	/**
	 * Construct the Effect.
	 * @param skill
	 * @param name
	 * @param period
	 * @param duration
	 */
	public PeriodicExpirableEffect(Skill skill, String name, long period, long duration) {
		super(skill, name, duration);
		this.period = period;
	}
	
	public PeriodicExpirableEffect(Skill skill, AlfCore plugin, String name, long period, long duration) {
		super(skill, plugin, name, duration);
		this.period = period;
	}
	
	/**
	 * Get the last tick time.
	 * @return
	 */
	public long getLastTickTime() {
		return this.lastTickTime;
	}
	
	/**
	 * The period for this effect.
	 */
	public long getPeriod() {
		return this.period;
	}
	
	/**
	 * Whether this effect is ready to tick.
	 */
	public boolean isReady() {
		return System.currentTimeMillis() >= this.lastTickTime + this.period;
	}
	
	/**
	 * Tick this effect.
	 */
	public void tick(CharacterTemplate character) {
		this.lastTickTime = System.currentTimeMillis();
		if (character instanceof Alf)
			tickAlf((Alf) character);
		else if (character instanceof Monster)
			tickMonster((Monster) character);
	}
	
}
