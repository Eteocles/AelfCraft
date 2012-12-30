package com.alf.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import com.alf.character.Alf;
import com.alf.skill.Skill;

/**
 * Describes an Event in which a skill is used.
 * Although other Character types can use Skills, only Players call it from a command.
 * @author Eteocles
 */
public class SkillUseEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final Skill skill;
	private final Alf alf;
	private int manaCost = 0;
	private int healthCost = 0;
	private int staminaCost = 0;
	private String[] args;
	private ItemStack reagentCost;
	private boolean cancelled = false;

	/**
	 * Construct the SkillUseEvent.
	 * @param activeSkill
	 * @param player
	 * @param alf
	 * @param manaCost
	 * @param healthCost
	 * @param staminaCost
	 * @param itemStack
	 * @param args
	 */
	public SkillUseEvent(Skill skill, Player player, Alf alf,
			int manaCost, int healthCost, int staminaCost, ItemStack reagentCost,
			String[] args) {
		this.player = player;
		this.skill = skill;
		this.alf = alf;
		this.args = args;
		this.manaCost = manaCost;
		this.healthCost = healthCost;
		this.reagentCost = reagentCost;
		this.staminaCost = staminaCost;
	}

	/**
	 * Get the arguments for the Skill used.
	 * @return
	 */
	public String[] getArgs()
	{	return this.args;	}

	/**
	 * Get the health expended.
	 * @return
	 */
	public int getHealthCost()
	{	return this.healthCost;	}

	/**
	 * Get the stamina expended.
	 * @return
	 */
	public int getStaminaCost()
	{	return this.staminaCost;	}

	/**
	 * Get the alf using the skill.
	 * @return
	 */
	public Alf getAlf()
	{	return this.alf; 	}
	
	/**
	 * Get the mana expended.
	 * @return
	 */
	public int getManaCost()
	{	return this.manaCost;	}

	/**
	 * Get the player.
	 * @return
	 */
	public Player getPlayer()
	{	return this.player;	}

	/**
	 * Get the items expended.
	 * @return
	 */
	public ItemStack getReagentCost() 
	{	return this.reagentCost;	}

	/**
	 * Get the skill used.
	 * @return
	 */
	public Skill getSkill()
	{	return this.skill;	}

	/**
	 * Whether the event is cancelled.
	 * @return
	 */
	public boolean isCancelled()
	{	return this.cancelled;	}

	/**
	 * Set the arguments for the skill.
	 * @param args
	 */
	public void setArgs(String[] args)
	{	this.args = args;	}

	/**
	 * Set whether the event is cancelled.
	 * @param val
	 */
	public void setCancelled(boolean val)
	{	this.cancelled = val;	}

	/**
	 * Set the health expenditure.
	 * @param healthCost
	 */
	public void setHealthCost(int healthCost)
	{	this.healthCost = healthCost;	}

	/**
	 * Set the stamina expenditure.
	 * @param staminaCost
	 */
	public void setStaminaCost(int staminaCost)
	{	this.staminaCost = staminaCost;	}

	/**
	 * Set the mana expenditure.
	 * @param manaCost
	 */
	public void setManaCost(int manaCost)
	{	this.manaCost = manaCost;	}

	/**
	 * Set the reagent expenditure.
	 * @param reagentCost
	 */
	public void setReagentCost(ItemStack reagentCost)
	{	this.reagentCost = reagentCost;	}

	/**
	 * Get the handlers for this event.
	 */
	public HandlerList getHandlers()
	{	return handlers;	}

	public static HandlerList getHandlerList() 
	{	return handlers;	}

}
