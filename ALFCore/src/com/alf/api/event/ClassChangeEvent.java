package com.alf.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.chararacter.Alf;
import com.alf.chararacter.classes.AlfClass;

/**
 * Describes an event in which an Alf changes classes.
 * @author Eteocles
 */
public class ClassChangeEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	protected boolean cancelled = false;
	protected final Alf alf;
	protected final AlfClass from;
	protected AlfClass to;
	private double cost;
	
	/**
	 * Construct the event.
	 * @param alf
	 * @param from
	 * @param to
	 * @param cost
	 */
	public ClassChangeEvent(Alf alf, AlfClass from, AlfClass to, double cost) {
		this.alf = alf;
		this.from = from;
		this.to = to;
		this.cost = cost;
	}
	
	/**
	 * Get the cost.
	 * @return
	 */
	public double getCost() {
		return this.cost;
	}

	/**
	 * Set the cost.
	 * @param cost
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * Whether the event is cancelled.
	 * @return
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * Set whether the event is cancelled.
	 * @param cancelled
	 */
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	/**
	 * Get the class switched from.
	 * @return
	 */
	public final AlfClass getFrom() {
		return this.from;
	}
	
	/**
	 * Get the class switched to.
	 * @return
	 */
	public AlfClass getTo() {
		return this.to;
	}
	
	/**
	 * Get the alf changing classes.
	 * @return
	 */
	public final Alf getAlf() {
		return this.alf;
	}

	/**
	 * Get the handlers for the event.
	 */
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}
