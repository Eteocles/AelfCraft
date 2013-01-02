package com.alf.character.effect.common;

import net.minecraft.server.v1_4_6.EntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.alf.character.Alf;
import com.alf.character.Pet;
import com.alf.character.effect.PeriodicEffect;
import com.alf.skill.Skill;

/**
 * An effect to teleport a pet to the owner if too far away.
 * @author Eteocles
 */
public class PetFollowEffect extends PeriodicEffect {

	public PetFollowEffect(Skill skill, long period) {
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

		Pet pet = alf.getPet();
		LivingEntity petEntity = pet.getEntity();

		//Override cancel navigation.
		if (pet.getAction() != 3)
			((CraftLivingEntity)petEntity).getHandle().getNavigation().g();

		if (petEntity instanceof Creature) {
			Creature creature = (Creature)alf.getPet().getEntity();
			if (creature.getTarget() == null || !(creature.getTarget() instanceof LivingEntity) && pet.getAction() != 1) {
				creature.setTarget(alf.getPlayer());
			} else if (pet.getAction() == 1)
				creature.setTarget(null);
		}
		follow(pet, alf);
	}

	/**
	 * Cause the creature to follow the Alf.
	 * @param creature
	 * @param alf
	 */
	private void follow(Pet pet, Alf alf) {
		LivingEntity entity = pet.getEntity();

		Location entityLoc = entity.getLocation();
		Location alfLoc = alf.getPlayer().getLocation();

		if (pet.getAction() == 0) {
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
}