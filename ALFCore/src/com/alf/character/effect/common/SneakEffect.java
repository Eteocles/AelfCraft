package com.alf.character.effect.common;

import org.bukkit.entity.Player;

import com.alf.character.Alf;
import com.alf.character.Monster;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.PeriodicExpirableEffect;
import com.alf.skill.Skill;
import com.alf.util.Messaging;

/**
 * A condition in which a player is sneaking.
 * @author Eteocles
 */
public class SneakEffect extends PeriodicExpirableEffect {

	/**
	 * Construct the effect.
	 * @param skill
	 * @param period
	 * @param duration
	 */
	public SneakEffect(Skill skill, long period, long duration) {
		super(skill, "Sneak", period, duration);
		this.types.add(EffectType.BENEFICIAL);
		this.types.add(EffectType.PHYSICAL);
		this.types.add(EffectType.SNEAK);
	}

	/**
	 * Apply the effect to the alf.
	 */
	public void applyToAlf(Alf alf) {
		super.applyToAlf(alf);
		Player player = alf.getPlayer();
		player.setSneaking(true);
	}

	/**
	 * Remove from the alf.
	 */
	public void removeFromAlf(Alf alf) {
		super.removeFromAlf(alf);
		Player player = alf.getPlayer();
		player.setSneaking(false);
		Messaging.send(player, "You are no longer sneaking!", new Object[0]);
	}

	/**
	 * Tick the effect on the alf.
	 */
	public void tickAlf(Alf alf) {
		alf.getPlayer().setSneaking(false);
		alf.getPlayer().setSneaking(true);
	}

	public void tickMonster(Monster monster) {}

}
