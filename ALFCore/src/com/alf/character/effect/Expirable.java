package com.alf.character.effect;

/**
 * Describes an Expirable type.
 * @author Eteocles
 */
public interface Expirable {

	/** Duration of the Expirable Effect. */
	public abstract long getDuration();
	/** Get the expiry time. */
	public abstract long getExpiry();
	/** Remaining Time for Effect */
	public abstract long getRemainingTime();
	/** Checks whether or not an Expired Effect is Expired. */
	public abstract boolean isExpired();
	/** Evaluates when the Effect is Expired. */
	public abstract void expire();
	
}
