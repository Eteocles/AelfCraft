package com.alf.chararacter.effect.common;

import org.bukkit.entity.Player;

import com.alf.chararacter.Alf;
import com.alf.chararacter.Monster;
import com.alf.chararacter.effect.EffectType;
import com.alf.chararacter.effect.PeriodicExpirableEffect;
import com.alf.skill.Skill;

/**
 * Describes a condition in which an entity is set on fire.
 * @author Eteocles
 */
public class CombustEffect extends PeriodicExpirableEffect {

	private final Player applier;
	private boolean expired = false;
	private int lastFireTickCount = -1;
	
	/**
	 * Construct the combust effect.
	 * @param skill
	 * @param applier
	 */
	public CombustEffect(Skill skill, Player applier) {
		super(skill, "Combust", 10L, 0L);
		this.types.add(EffectType.FIRE);
		setPersistent(true);
		this.applier = applier;
	}
	
	/**
	 * Apply the effect to a given monster.
	 */
	@Override
	public void applyToMonster(Monster monster) {
		super.applyToMonster(monster);
		this.lastFireTickCount = monster.getEntity().getFireTicks();
	}
	
	/**
	 * Apply the effect to a given alf.
	 */
	public void applyToAlf(Alf alf) {
		super.applyToAlf(alf);
		this.lastFireTickCount = alf.getPlayer().getFireTicks();
	}
	
	/**
	 * Get the player applying the effect.
	 * @return
	 */
	public Player getApplier() {
		return this.applier;
	}
	
	/**
	 * Whether the effect expired.
	 * @return
	 */
	public boolean isExpired() {
		return this.expired;
	}
	
	/**
	 * Remove the effect from the given monster.
	 * @param monster
	 */
	public void removeFromMonster(Monster monster) {
		super.removeFromMonster(monster);
	}
	
	/**
	 * Remove the effect from the given alf.
	 * @param alf
	 */
	public void removeFromAlf(Alf alf) {
		super.removeFromAlf(alf);
	}
	
	/**
	 * Tick the given monster.
	 * @param m
	 */
	@Override
	public void tickMonster(Monster monster) {
		int fireTicks = monster.getEntity().getFireTicks();
		
		if (fireTicks == 0) 
			this.expired = true;
		
		if (this.lastFireTickCount - fireTicks >= 10) 
			monster.getEntity().setNoDamageTicks(0);
		
		this.lastFireTickCount = fireTicks;
	}

	/**
	 * Tick the given alf.
	 * @param a
	 */
	@Override
	public void tickAlf(Alf alf) {
		Player player = alf.getPlayer();
		
		if (player.getFireTicks() == 0)
			this.expired = true;
		
		int fireTicks = player.getFireTicks();
		if (this.lastFireTickCount - fireTicks >= 10)
			player.setNoDamageTicks(0);
		
		this.lastFireTickCount = fireTicks;
	}

}
