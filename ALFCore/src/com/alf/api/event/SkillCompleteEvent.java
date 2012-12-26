package com.alf.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.api.SkillResult;
import com.alf.chararacter.Alf;
import com.alf.skill.ActiveSkill;

public class SkillCompleteEvent extends Event {

	public SkillCompleteEvent(Alf alf, ActiveSkill activeSkill, SkillResult sr) {
		throw new Error("Implement me!");
	}

	@Override
	public HandlerList getHandlers() {
		throw new Error("Implement me!");
	}

}
