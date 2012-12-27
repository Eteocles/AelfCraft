package com.alf.api.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.chararacter.Alf;
import com.alf.chararacter.classes.AlfClass;

/**
 * Describes an event in which a Player's experience level changes.
 * @author Eteocles
 */
public class ExperienceChangeEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	protected boolean cancelled = false;
	protected final Alf alf;
	protected final AlfClass alfClass;
	protected double expChange;
	protected final AlfClass.ExperienceType source;
	protected final Location location;

	/**
	 * Construct the event.
	 * @param alf
	 * @param alfClass
	 * @param expChange
	 * @param source
	 * @param location
	 */
	public ExperienceChangeEvent(Alf alf, AlfClass alfClass, double expChange, AlfClass.ExperienceType source, Location location) {
		this.alf = alf;
		this.expChange = expChange;
		this.source = source;
		this.alfClass = alfClass;
		this.location = location;
	}

	/**
	 * Get amount of exp being changed.
	 * @return
	 */
	public double getExpChange() {
		return this.expChange;
	}

	/**
	 * Get the alf whose exp is changing.
	 * @return
	 */
	public final Alf getAlf() {
		return this.alf;
	}

	/**
	 * Get the source of the experience change.
	 * @return
	 */
	public final AlfClass.ExperienceType getSource() {
		return this.source;
	}

	/**
	 * Get the location of the experience change.
	 * @return
	 */
	public final Location getLocation() {
		return this.location;
	}

	/**
	 * Whether the experience change is cancelled.
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * Set whether the experience change is cancelled.
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * Set the amount of exp being gained.
	 * @param exp
	 */
	public void setExpGain(double exp) {
		this.expChange = exp;
	}

	/**
	 * Get the Alf class.
	 * @return
	 */
	public AlfClass getAlfClass() {
		return this.alfClass;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
