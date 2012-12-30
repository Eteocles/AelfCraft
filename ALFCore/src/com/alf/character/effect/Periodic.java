package com.alf.character.effect;

import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.character.Monster;

/**
 * Describes a Periodic type.
 * @author Eteocles
 */
public interface Periodic {

	/** Check the last time at which tick was evaluated. */
	public abstract long getLastTickTime();
	/** Get the total Period time of this Effect. */
	public abstract long getPeriod();
	/** Determine whether or not enough time has elapsed for a Periodic Effect to evaluate. */
	public abstract boolean isReady();
	/** Ticks a CharacterTemplate. */
	public abstract void tick(CharacterTemplate ct);
	/** Ticks a Monster. */
	public abstract void tickMonster(Monster m);
	/** Ticks an Alf. */
	public abstract void tickAlf(Alf a);
	
}
