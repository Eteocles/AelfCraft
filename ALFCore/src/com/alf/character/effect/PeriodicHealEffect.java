package com.alf.character.effect;

import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.event.AlfRegainHealthEvent;
import com.alf.character.Alf;
import com.alf.character.Monster;
import com.alf.skill.Skill;

/**
 * A condition in which a player is periodically healed.
 * @author Eteocles
 */
public class PeriodicHealEffect extends PeriodicExpirableEffect {

	  private int tickHealth;
	  private final Player applier;
	  private final Alf healer;

	  /**
	   * Construct the effect.
	   * @param plugin
	   * @param name
	   * @param period
	   * @param duration
	   * @param tickHealth
	   * @param applier
	   */
	  public PeriodicHealEffect(AlfCore plugin, String name, long period, long duration, int tickHealth, Player applier) {
	    super(null, plugin, name, period, duration);
	    this.tickHealth = tickHealth;
	    this.applier = applier;
	    this.types.add(EffectType.BENEFICIAL);
	    this.types.add(EffectType.HEAL);
	    this.healer = plugin.getCharacterManager().getAlf(applier);
	  }

	  public PeriodicHealEffect(Skill skill, String name, long period, long duration, int tickHealth, Player applier) {
	    super(skill, name, period, duration);
	    this.tickHealth = tickHealth;
	    this.applier = applier;
	    this.types.add(EffectType.BENEFICIAL);
	    this.types.add(EffectType.HEAL);
	    this.healer = this.plugin.getCharacterManager().getAlf(applier);
	  }

	  public Player getApplier() {
	    return this.applier;
	  }

	  public int getTickDamage() {
	    return this.tickHealth;
	  }

	  public void setTickHealth(int tickHealth) {
	    this.tickHealth = tickHealth;
	  }

	  public void tickAlf(Alf alf) {
	    AlfRegainHealthEvent alfEvent = new AlfRegainHealthEvent(alf, this.tickHealth, this.skill, this.healer);
	    this.plugin.getServer().getPluginManager().callEvent(alfEvent);
	    if (alfEvent.isCancelled())
	      return;
	    alf.setHealth(alf.getHealth() + alfEvent.getAmount());
	    alf.syncHealth();
	  }

	  public void tickMonster(Monster monster) {
	    int maxHealth = monster.getMaxHealth();
	    monster.setHealth(this.tickHealth + monster.getHealth());
	    if (monster.getHealth() > maxHealth)
	      monster.setHealth(maxHealth);
	  }

}
