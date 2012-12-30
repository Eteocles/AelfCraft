package com.alf.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.character.party.AlfParty;

/**
 * Describes an event in which an Alf joins a party.
 * @author Eteocles
 *
 */
public class AlfJoinPartyEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private final Alf alf;
	private final AlfParty party;

	/**
	 * Construct the event.
	 * @param alf
	 * @param alfParty
	 */
	public AlfJoinPartyEvent(Alf alf, AlfParty alfParty) {
		this.alf = alf;
		this.party = alfParty;
	}

	public Alf getAlf() {
		return this.alf;
	}

	public AlfParty getParty() {
		return this.party;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean val) {
		this.cancelled = val;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
