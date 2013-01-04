package com.alf.character.effect.common;

import com.alf.character.Alf;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.ExpirableEffect;
import com.alf.skill.DelayedSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillType;

/**
 * A condition in which a character can not use skills.
 * @author Eteocles
 */
public class SilenceEffect extends ExpirableEffect
{
  private final String expireText = "$1 is no longer silenced!";

  public SilenceEffect(Skill skill, long duration) {
    super(skill, "Silence", duration);
    this.types.add(EffectType.DISPELLABLE);
    this.types.add(EffectType.HARMFUL);
    this.types.add(EffectType.SILENCE);
  }

  /**
   * Apply the effect to an Alf.
   */
  public void applyToAlf(Alf alf)
  {
    super.applyToAlf(alf);
    DelayedSkill dSkill = alf.getDelayedSkill();
    if ((dSkill != null) && (dSkill.getSkill().isType(SkillType.SILENCABLE)))
      alf.cancelDelayedSkill();
  }

  /**
   * Remove from the Alf.
   */
  public void removeFromAlf(Alf alf)
  {
    super.removeFromAlf(alf);
    broadcast(alf.getPlayer().getLocation(), expireText, new Object[] { alf.getPlayer().getDisplayName() });
  }
}