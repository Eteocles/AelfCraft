package com.alf.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.character.party.AlfParty;

/**
 * Describes an event in which a player chats in party.
 * @author Eteocles
 */
public class PartyChatEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Alf sender;
	private final AlfParty party;
	private String message = null;
	private boolean cancelled = false;

	/**
	 * Construct the event.
	 * @param sender
	 * @param party
	 * @param message
	 */
	public PartyChatEvent(Alf sender, AlfParty party, String message) {
		this.sender = sender;
		this.party = party;
		this.message = message;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Alf getSender() {
		return this.sender;
	}

	public AlfParty getParty() {
		return this.party;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

}
