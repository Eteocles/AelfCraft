package com.alf.character.effect;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.character.Monster;
import com.alf.skill.Skill;

/**
 * Describes a periodic condition that acts on a Character.
 * @author Eteocles
 */
public class PeriodicEffect extends Effect implements Periodic {
	
	private final long period;
	protected long lastTickTime = 0L;
	
	/**
	 * Constructs a Periodic Effect.
	 * @param skill
	 * @param name
	 * @param period
	 */
	public PeriodicEffect(Skill skill, String name, long period) {
		super(skill, name);
		this.period = period;
	}
	
	public PeriodicEffect(AlfCore plugin, String name, long period) {
		super(plugin, null, name, new EffectType[0]);
		this.period = period;
	}
	
	/**
	 * Get the last time moment of ticking.
	 */
	public long getLastTickTime() {
		return this.lastTickTime;
	}
	
	/**
	 * Get the period of the effect oscillation.
	 */
	public long getPeriod() {
		return this.period;
	}
	
	/**
	 * Whether or not the effect is ready to be ticked again.
	 */
	public boolean isReady() {
		return System.currentTimeMillis() >= this.lastTickTime + this.period;
	}
	
	/**
	 * Tick this effect on a Character.
	 */
	public void tick(CharacterTemplate character) {
		this.lastTickTime = System.currentTimeMillis();
		if (character instanceof Alf)
			tickAlf((Alf) character);
		else if (character instanceof Monster)
			tickMonster((Monster) character);
	}
	
	public void tickMonster(Monster m) {}
	
	public void tickAlf(Alf a) {}
	
}
