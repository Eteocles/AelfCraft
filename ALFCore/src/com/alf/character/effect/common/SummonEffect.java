package com.alf.character.effect.common;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;

import com.alf.character.Alf;
import com.alf.character.Monster;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.ExpirableEffect;
import com.alf.character.effect.PeriodicEffect;
import com.alf.skill.Skill;

/**
 * Describes a condition in which a monster is summoned.
 * @author Eteocles
 */
public class SummonEffect extends ExpirableEffect {
	
	private Alf summoner;
	private final String expireText;

	/**
	 * Construct the Summon Effect.
	 * @param skill
	 * @param duration
	 * @param summoner
	 * @param expireText
	 */
	public SummonEffect(Skill skill, long duration, Alf summoner, String expireText)
	{
		super(skill, "Summon", duration);
		this.summoner = summoner;
		this.expireText = expireText;
		this.types.add(EffectType.DISPELLABLE);
		this.types.add(EffectType.BENEFICIAL);
	}
	
	/**
	 * Apply the effect to a monster.
	 */
	public void applyToMonster(Monster monster)
	{
		super.applyToMonster(monster);
		this.summoner.getSummons().add(monster);
		FollowEffect fEffect = new FollowEffect(this.skill, 1500L);
		this.summoner.addEffect(fEffect);
	}

	/**
	 * Remove the effect from a monster.
	 */
	public void removeFromMonster(Monster monster) {
		super.removeFromMonster(monster);
		LivingEntity creature = monster.getEntity();
		this.summoner.getSummons().remove(creature);
		broadcast(creature.getLocation(), this.expireText, new Object[0]);

		if (!creature.getWorld().getChunkAt(creature.getLocation()).isLoaded()) 
			creature.getWorld().loadChunk(creature.getWorld().getChunkAt(creature.getLocation()));
		creature.remove();

		for (Monster mon : this.summoner.getSummons()) 
			if (mon.hasEffect(this.name))
				return;

		this.summoner.removeEffect(this.summoner.getEffect("Follow"));
	}

	/**
	 * Get the summoning alf.
	 * @return
	 */
	public Alf getSummoner() {
		return this.summoner;
	}
	
	/**
	 * Describes a condition in which all summons teleport and follow a player.
	 * @author Eteocles
	 */
	public class FollowEffect extends PeriodicEffect
	{
		/**
		 * Construct the Follow Effect.
		 * @param skill
		 * @param period
		 */
		public FollowEffect(Skill skill, long period) {
			super(skill, "Follow", period);
			setPersistent(true);
		}

		/**
		 * Apply the effect to an Alf.
		 */
		public void applyToAlf(Alf alf) {
			super.applyToAlf(alf);
		}

		/**
		 * Remove the effect from an Alf.
		 */
		public void removeFromAlf(Alf alf) {
			super.removeFromAlf(alf);
		}

		/**
		 * Tick the effect on the Alf.
		 */
		public void tickAlf(Alf alf) {
			super.tickAlf(alf);
			for (Monster monster : alf.getSummons())
				if (monster.hasEffect("Summon")) {
					Creature creature = (Creature)monster.getEntity();
					if (creature.getTarget() == null || !(creature.getTarget() instanceof LivingEntity))
						follow(creature, alf);
				}
		}

		/**
		 * Cause the creature to follow the Alf.
		 * @param creature
		 * @param alf
		 */
		private void follow(Creature creature, Alf alf) {
			Location creatureLoc = creature.getLocation();
			Location alfLoc = alf.getPlayer().getLocation();
			if (!creatureLoc.getWorld().equals(alfLoc.getWorld()) || creatureLoc.distanceSquared(alfLoc) > 400.0D) {
				creature.teleport(alfLoc);
				return;
			}
		}
	}
}
