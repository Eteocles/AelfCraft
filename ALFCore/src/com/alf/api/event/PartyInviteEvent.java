package com.alf.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.character.party.AlfParty;

/**
 * Describes an event in which an Alf is invited to a Party.
 * @author Eteocles
 */
public class PartyInviteEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Alf alf;
	private final AlfParty party;

	public PartyInviteEvent(Alf alf, AlfParty alfParty)
	{
		this.alf = alf;
		this.party = alfParty;
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

}
