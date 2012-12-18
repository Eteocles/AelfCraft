package com.alf.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.chararacter.Alf;
import com.alf.chararacter.CharacterTemplate;

public class AlfKillCharacterEvent extends Event {

	public AlfKillCharacterEvent(CharacterTemplate character, Alf alf) {
		throw new Error("Implement me!");
	}

	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return null;
	}

}
