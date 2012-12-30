package com.alf.character.effect;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.alf.character.Alf;
import com.alf.character.Monster;
import com.alf.skill.Skill;
import com.alf.skill.SkillType;

/**
 * Describes a periodic damage type condition.
 * @author Eteocles
 */
public class PeriodicDamageEffect extends PeriodicExpirableEffect {

	protected int tickDamage;
	protected final Player applier;
	protected final Alf applyAlf;
	private final boolean knockback;
	
	/**
	 * Construct the effect.
	 * @param skill
	 * @param name
	 * @param period
	 * @param duration
	 * @param tickDamage
	 * @param applier
	 * @param knockback
	 */
	public PeriodicDamageEffect(Skill skill, String name, long period, long duration, 
			int tickDamage, Player applier, boolean knockback) {
		super(skill, name, period, duration);
		this.tickDamage = tickDamage;
		this.applier = applier;
		this.applyAlf = this.plugin.getCharacterManager().getAlf(applier);
		this.types.add(EffectType.HARMFUL);
		this.knockback = knockback;
	}

	public PeriodicDamageEffect(Skill skill, String name, long period, long duration, 
			int tickDamage, Player applier) {
		this(skill, name, period, duration, tickDamage, applier, false);
	}
	
	/**
	 * Get the app
	 * @return
	 */
	public Player getApplier() {
		return this.applier;
	}
	
	/**
	 * Get the Alf this Alf is applied to.
	 * @return
	 */
	public Alf getApplierAlf() {
		return this.applyAlf;
	}
	
	/**
	 * Get the amount of damage each tick.
	 * @return
	 */
	public int getTickDamage() {
		return this.tickDamage;
	}
	
	/**
	 * Set the amount of damage dealt each tick.
	 * @param tickDamage
	 */
	public void setTickDamage(int tickDamage) {
		this.tickDamage = tickDamage;
	}
	
	/**
	 * Tick the effect on the given monster.
	 */
	public void tickMonster(Monster monster) {
		this.skill.addSpellTarget(monster.getEntity(), this.applyAlf);
		Skill.damageEntity(monster.getEntity(), this.applier, this.tickDamage, 
				this.skill.isType(SkillType.PHYSICAL) ? EntityDamageEvent.DamageCause.ENTITY_ATTACK : EntityDamageEvent.DamageCause.MAGIC,
				this.knockback);
	}
	
	/**
	 * Tick the effect on the given alf.
	 */
	public void tickAlf(Alf alf) {
		Player player = alf.getPlayer();
		
		if (Skill.damageCheck(this.applier, player)) {
			this.skill.addSpellTarget(player, this.applyAlf);
			Skill.damageEntity(player, this.applier, this.tickDamage,
					this.skill.isType(SkillType.PHYSICAL) ? EntityDamageEvent.DamageCause.ENTITY_ATTACK : EntityDamageEvent.DamageCause.MAGIC,
					this.knockback);
		}
	}
	
}
