package com.alf.character.effect.common;


import net.minecraft.server.v1_4_5.EntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftLivingEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.alf.character.Alf;
import com.alf.character.effect.PeriodicEffect;
import com.alf.skill.Skill;

/**
 * An effect to teleport a pet to the owner if too far away.
 * @author Eteocles
 */
public class FollowEffect extends PeriodicEffect {

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
			LivingEntity pet = alf.getPet().getEntity();
			
			((CraftLivingEntity)pet).getHandle().getNavigation().g();
			
			if (pet instanceof Creature) {
				Creature creature = (Creature)alf.getPet().getEntity();
				if (creature.getTarget() == null || !(creature.getTarget() instanceof LivingEntity)) {
					creature.setTarget(alf.getPlayer());
				}
			}
			follow(pet, alf);
		}

	/**
	 * Cause the creature to follow the Alf.
	 * @param creature
	 * @param alf
	 */
	private void follow(LivingEntity entity, Alf alf) {
		
		Location entityLoc = entity.getLocation();
		Location alfLoc = alf.getPlayer().getLocation();
		
		Vector vector = alfLoc.toVector().subtract(entity.getEyeLocation().getDirection());
		
		EntityLiving eL = ((CraftLivingEntity)entity).getHandle();
		eL.getControllerMove().a(vector.getX(), vector.getY(), vector.getZ(), 0.8F);

		if (Math.random() < 0.15F)
			eL.getControllerJump().a();
		
		if (!entityLoc.getWorld().equals(alfLoc.getWorld()) || entityLoc.distanceSquared(alfLoc) > 200.0D) {
			entity.teleport(alfLoc);
			return;
		}
	}
}