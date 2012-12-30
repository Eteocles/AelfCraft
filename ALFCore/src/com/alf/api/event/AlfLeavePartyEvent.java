package com.alf.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.character.party.AlfParty;

/**
 * Describes an event in which an Alf leaves a party.
 * @author Eteocles
 *
 */
public class AlfLeavePartyEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Alf alf;
	private final AlfParty party;
	private final LeavePartyReason reason;
	private boolean cancelled = false;

	/**
	 * Construct the event.
	 * @param alf
	 * @param alfParty
	 * @param reason
	 */
	public AlfLeavePartyEvent(Alf alf, AlfParty alfParty, LeavePartyReason reason) {
		this.alf = alf;
		this.party = alfParty;
		this.reason = reason;
	}

	public Alf getAlf() {
		return this.alf;
	}

	public AlfParty getParty() {
		return this.party;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public boolean isCancelled() {
		return (this.cancelled) && (isCancellable());
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isCancellable() {
		return (this.reason != LeavePartyReason.DISCONNECT) && (this.reason != LeavePartyReason.SYSTEM);
	}

	public LeavePartyReason getReason() {
		return this.reason;
	}

	public static enum LeavePartyReason {
		COMMAND, 
		DISCONNECT, 
		KICK, 
		SYSTEM;
	}

}
