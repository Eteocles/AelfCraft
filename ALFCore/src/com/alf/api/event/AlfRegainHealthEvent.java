package com.alf.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.alf.character.Alf;
import com.alf.skill.Skill;

/**
 * An event in which an Alf regains health.
 * @author Eteocles
 */
public class AlfRegainHealthEvent extends Event
  implements Cancellable
{
  private static final HandlerList handlers = new HandlerList();
  private int amount;
  private final Alf alf;
  private final Alf healer;
  private final Skill skill;
  private boolean cancelled = false;

  /**
   * Regain the Alf's health.
   * @param alf
   * @param amount
   * @param skill
   */
  public AlfRegainHealthEvent(Alf alf, int amount, Skill skill) {
    this(alf, amount, skill, null);
  }

  public AlfRegainHealthEvent(Alf alf, int amount, Skill skill, Alf healer) {
    this.amount = amount;
    this.alf = alf;
    this.skill = skill;
    this.healer = healer;
  }

  /**
   * Amount healed.
   * @return
   */
  public int getAmount()
  {
    return this.amount;
  }

  /**
   * Get the alf healed.
   * @return
   */
  public Alf getAlf()
  {
    return this.alf;
  }

  /**
   * Get the skill used to heal.
   * @return
   */
  public Skill getSkill()
  {
    return this.skill;
  }

  /**
   * Whether the event was cancelled.
   */
  public boolean isCancelled()
  {
    return this.cancelled;
  }

  /**
   * Set the amount of health regained.
   * @param amount
   */
  public void setAmount(int amount)
  {
    this.amount = amount;
  }

  /**
   * Set whether the event was cancelled.
   */
  public void setCancelled(boolean cancelled)
  {
    this.cancelled = cancelled;
  }

  /**
   * Get the healer.
   * @return
   */
  public Alf getHealer()
  {
    return this.healer;
  }

  public HandlerList getHandlers()
  {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}