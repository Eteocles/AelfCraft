package com.alf.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.skill.Skill;

/**
 * Describes an event in which an Alf regains mana.
 * @author Eteocles
 */
public class AlfRegainManaEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private int amount;
	private final Alf alf;
	private final Skill skill;
	private boolean cancelled = false;

	/**
	 * Construct the event.
	 * @param alf
	 * @param amount
	 * @param skill
	 */
	public AlfRegainManaEvent(Alf alf, int amount, Skill skill) {
		this.alf = alf;
		this.amount = amount;
		this.skill = skill;
	}

	/**
	 * Get the amount of mana being restored.
	 * @return
	 */
	public int getAmount() {
		return this.amount;
	}

	/**
	 * Get the Alf gaining mana.
	 * @return
	 */
	public Alf getAlf() {
		return this.alf;
	}

	/**
	 * Get the skill used to regain mana, if any.
	 * @return
	 */
	public Skill getSkill() {
		return this.skill;
	}

	/**
	 * Whether the event is cancelled.
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * Set the amount of mana being restored.
	 * @param amount
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * Set whether the event is cancelled.
	 */
	public void setCancelled(boolean val) {
		this.cancelled = val;
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
