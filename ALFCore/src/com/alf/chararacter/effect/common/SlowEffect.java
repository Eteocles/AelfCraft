package com.alf.chararacter.effect.common;

import org.bukkit.entity.Player;

import com.alf.chararacter.Alf;
import com.alf.chararacter.Monster;
import com.alf.chararacter.effect.EffectType;
import com.alf.chararacter.effect.ExpirableEffect;
import com.alf.skill.Skill;
import com.alf.util.Messaging;

/**
 * A condition in which an entity is slowed.
 * @author Eteocles
 */
public class SlowEffect extends ExpirableEffect {

	private final String applyText;
	private final String expireText;
	private final Alf applier;
	
	/**
	 * Construct the slow effect.
	 * @param skill
	 * @param name
	 * @param duration
	 * @param amplifier
	 * @param swing
	 * @param string2
	 * @param string3
	 * @param alf
	 */
	public SlowEffect(Skill skill, String name, long duration, int amplifier, boolean swing, 
			String applyText, String expireText, Alf applier) {
		super(skill, name, duration);
		this.types.add(EffectType.DISPELLABLE);
		this.types.add(EffectType.HARMFUL);
		this.types.add(EffectType.SLOW);
		this.applyText = applyText;
		this.expireText = expireText;
		this.applier = applier;
		int tickDuration = (int)(duration / 1000L) * 20;
		
		addMobEffect(2, tickDuration, amplifier, false);
		addMobEffect(8, tickDuration, -amplifier, false);
		
		if (swing)
			addMobEffect(4, tickDuration, amplifier, false);
	}
	
	public SlowEffect(Skill skill, long duration, int amplifier, boolean swing, String applyText, String expireText, Alf applier)
	{
		this(skill, "Slow", duration, amplifier, swing, expireText, expireText, applier);
	}
	
	/**
	 * Apply the effect to a given alf.
	 */
	public void applyToAlf(Alf alf) {
		super.applyToAlf(alf);
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), this.expireText, new Object[] { player.getDisplayName() });
	}
	
	/**
	 * Remove the effect from a given alf.
	 */
	public void removeFromAlf(Alf alf) {
		super.removeFromAlf(alf);
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), this.expireText, new Object[] {
			player.getDisplayName()
		});
	}
	
	/**
	 * Apply the effect to a given monster.
	 */
	public void applyToMonster(Monster monster) {
		super.applyToMonster(monster);
		broadcast(monster.getEntity().getLocation(), this.applyText, new Object[] { 
			Messaging.getLivingEntityName(monster), this.applier.getPlayer().getDisplayName()
		});
	}
	
	/**
	 * Remove the effect from a given monster.
	 */
	public void removeFromMonster(Monster monster) {
		super.removeFromMonster(monster);
		broadcast(monster.getEntity().getLocation(), this.expireText, new Object[] { 
			Messaging.getLivingEntityName(monster) });
	}

}
