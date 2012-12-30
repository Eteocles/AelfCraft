package com.alf.character.effect;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.Monster;
import com.alf.skill.Skill;

/**
 * Describes a condition that expires after a period of time.
 * @author Eteocles
 */
public class ExpirableEffect extends Effect implements Expirable {

	private final long duration;
	private long expireTime;
	
	/**
	 * Construct an Expirable effect.
	 * @param skill
	 * @param name
	 * @param duration
	 */
	public ExpirableEffect(Skill skill, String name, long duration) {
		super(skill, name);
		this.duration = duration;
	}
	
	public ExpirableEffect(Skill skill, AlfCore plugin, String name, long duration) {
		super(plugin, skill, name, new EffectType[0]);
		this.duration = duration;
	}
	
	/**
	 * Apply the Expirable Effect to the Monster.
	 * @param monster
	 */
	public void applyToMonster(Monster monster) {
		super.applyToMonster(monster);
		this.expireTime = (this.applyTime + this.duration);
	}
	
	/**
	 * Apply the Expirable Effect to the Alf.
	 * @param alf
	 */
	public void applyToAlf(Alf alf) {
		super.applyToAlf(alf);
		this.expireTime = (this.applyTime + this.duration);
	}
	
	/**
	 * Get the time of this Effect's application.
	 * @return
	 */
	public long getApplyTime() {
		return this.applyTime;
	}
	
	/**
	 * Get the duration of this Effect.
	 * @return
	 */
	public long getDuration() {
		return this.duration;
	}

	/**
	 * Get the expiry time for this Effect.
	 * @return
	 */
	public long getExpiry() {
		return this.expireTime;
	}

	/**
	 * Get the remaining time for this Effect.
	 * @return
	 */
	public long getRemainingTime() {
		return this.expireTime - System.currentTimeMillis();
	}

	/**
	 * Whether the effect is expired.
	 * @return
	 */
	public boolean isExpired() {
		if (isPersistent())
			return false;
		return System.currentTimeMillis() >= getExpiry();
	}

	/**
	 * Force the effect to expire.
	 */
	public void expire() {
		this.expireTime = System.currentTimeMillis();
	}
	
	/**
	 * Remove this effect from the Monster.
	 * @param monster
	 */
	public void removeFromMonster(Monster monster) {
		super.removeFromMonster(monster);
	}
	
	/**
	 * Remove this effect from the Alf.
	 * @param alf
	 */
	public void removeFromAlf(Alf alf) {
		super.removeFromAlf(alf);
	}
	
}
