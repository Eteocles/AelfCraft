package com.alf.character.effect.common;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.alf.character.Alf;
import com.alf.character.Monster;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.PeriodicExpirableEffect;
import com.alf.skill.Skill;

/**
 * Describes a stun effect.
 * @author Eteocles
 */
public class StunEffect extends PeriodicExpirableEffect {
  private static final long period = 100L;
  private final String stunApplyText = "$1 is stunned!";
  private final String stunExpireText = "$1 is no longer stunned!";
  private Location loc;

  /**
   * Construct the effect.
   * @param skill
   * @param duration
   */
  public StunEffect(Skill skill, long duration) {
    super(skill, "Stun", period, duration);
    this.types.add(EffectType.STUN);
    this.types.add(EffectType.HARMFUL);
    this.types.add(EffectType.PHYSICAL);
    this.types.add(EffectType.DISABLE);
    addMobEffect(9, (int)(duration / 1000L) * 20, 127, false);
  }

  /**
   * Apply the alf.
   */
  public void applyToAlf(Alf alf) {
    super.applyToAlf(alf);
    Player player = alf.getPlayer();
    this.loc = alf.getPlayer().getLocation();
    broadcast(player.getLocation(), stunApplyText, new Object[] { player.getDisplayName() });
  }

  /**
   * Remove the alf.
   */
  public void removeFromAlf(Alf alf) {
    super.removeFromAlf(alf);
    Player player = alf.getPlayer();
    broadcast(player.getLocation(), stunExpireText, new Object[] { player.getDisplayName() });
  }

  /**
   * Tick the effect on an alf.
   */
  public void tickAlf(Alf alf) {
    Location location = alf.getPlayer().getLocation();
    if (location == null)
      return;
    if ((location.getX() != this.loc.getX()) || (location.getY() != this.loc.getY()) || (location.getZ() != this.loc.getZ())) {
      this.loc.setYaw(location.getYaw());
      this.loc.setPitch(location.getPitch());
      alf.getPlayer().teleport(this.loc);
    }
  }

  public void tickMonster(Monster monster) {}
}
