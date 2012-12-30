package com.alf.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.character.classes.AlfClass;

/**
 * Describes an event in which a player changes level.
 * @author Eteocles
 */
public class AlfChangeLevelEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final int from;
	private final int to;
	private final Alf alf;
	private final AlfClass alfClass;
	
	/**
	 * Construct the event.
	 * @param alf
	 * @param alfClass
	 * @param from
	 * @param to
	 */
	public AlfChangeLevelEvent(Alf alf, AlfClass alfClass, int from, int to) {
		this.alfClass = alfClass;
		this.from = from;
		this.to = to;
		this.alf = alf;
	}
	
	/**
	 * Get the level being changed from.
	 * @return
	 */
	public final int getFrom() {
		return this.from;
	}

	/**
	 * Get the alf whose level is changing.
	 * @return
	 */
	public Alf getAlf() {
		return this.alf;
	}

	/**
	 * Get the level being changed to.
	 * @return
	 */
	public final int getTo() {
		return this.to;
	}

	/**
	 * Get the alf's class which level is changing.
	 * @return
	 */
	public AlfClass getAlfClass() {
		return this.alfClass;
	}

	/**
	 * Get the handlers.
	 */
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
