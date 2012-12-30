package com.alf.character.effect.common;

import org.bukkit.entity.Player;

import com.alf.character.Alf;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.ExpirableEffect;
import com.alf.skill.Skill;

/**
 * Speed up an affected entity.
 * @author Eteocles
 */
public class QuickenEffect extends ExpirableEffect {
	private final String applyText;
	private final String expireText;

	/**
	 * Construct the effect.
	 * @param skill
	 * @param name
	 * @param duration
	 * @param amplifier
	 * @param applyText
	 * @param expireText
	 */
	public QuickenEffect(Skill skill, String name, long duration, int amplifier, String applyText, String expireText)
	{
		super(skill, name, duration);
		this.types.add(EffectType.DISPELLABLE);
		this.types.add(EffectType.BENEFICIAL);
		this.types.add(EffectType.MAGIC);
		addMobEffect(1, (int)(duration / 1000L) * 20, amplifier, false);
		this.applyText = applyText;
		this.expireText = expireText;
	}

	/**
	 * Apply this effect to a given alf.
	 */
	public void applyToAlf(Alf alf) {
		super.applyToAlf(alf);
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), this.applyText, new Object[] { player.getDisplayName() });
	}

	/**
	 * Remove this effect from a given alf.
	 */
	public void removeFromAlf(Alf alf){
		super.removeFromAlf(alf);
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), this.expireText, new Object[] { player.getDisplayName() });
	}
}
