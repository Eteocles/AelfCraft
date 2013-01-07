package com.alf.character.effect.common;

import org.bukkit.entity.Player;

import com.alf.character.Alf;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.ExpirableEffect;
import com.alf.skill.Skill;

/**
 * A ward effect that improves one's armor rating.
 * @author Eteocles
 */
public class WardEffect extends ExpirableEffect {

	private final String applyText;
	private final String expireText;
	private double armorRating;

	/**
	 * Construct the effect.
	 * @param skill
	 * @param name
	 * @param duration
	 * @param armorRating
	 */
	public WardEffect(Skill skill, String name, long duration, double armorRating) {
		super(skill, name, duration);
		this.types.add(EffectType.WARD);
		this.types.add(EffectType.BENEFICIAL);
		
		this.applyText = "$1 is now protected by a ward!";
		this.expireText = "$1 is no longer warded!";
		
		this.armorRating = armorRating;
	}

	public double getArmorRating() {
		return this.armorRating;
	}

	public void setArmorRating(double armorRating) {
		this.armorRating = armorRating;
	}
	
	public void applyToAlf(Alf alf) {
		super.applyToAlf(alf);
	    Player player = alf.getPlayer();
	    broadcast(player.getLocation(), this.applyText, new Object[] { player.getDisplayName() });
	}
	
	public void removeFromAlf(Alf alf) {
		super.removeFromAlf(alf);
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), this.expireText, new Object[] { player.getDisplayName() });
	}

}
