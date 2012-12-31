package com.alf.skill.skills;

import net.minecraft.server.v1_4_5.EntityLiving;

import org.bukkit.craftbukkit.v1_4_5.entity.CraftLivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.Pet;
import com.alf.character.effect.common.FollowEffect;
import com.alf.character.effect.common.PetEffect;
import com.alf.skill.SkillType;
import com.alf.skill.TargetedSkill;

public class SkillTame extends TargetedSkill {

	private AlfCore plugin;

	public SkillTame(AlfCore plugin) {
		super(plugin, "Tame");
		setDescription("You tame a mob and make it your pet!");
		setUsage("/skill tame");
		setArgumentRange(0,0);
		setIdentifiers(new String[] {"skill tame"});
		setTypes(new SkillType[] {SkillType.UNBINDABLE});
		this.plugin = plugin;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}


	@Override
	public SkillResult use(Alf alf, LivingEntity target, String[] args) {
		broadcastExecuteText(alf, target);
		
		if (alf.getPet() != null)
			return SkillResult.FAIL;
		
		switch (target.getType()) {
		case MAGMA_CUBE:
		case SLIME:
			((Slime)target).setSize(1);
		case CHICKEN:
		case COW:
		case MUSHROOM_COW:
		case OCELOT:
		case PIG:
		case SHEEP:
		case SKELETON:
		case VILLAGER:
		case WOLF:	
			((Ageable)target).setBaby();
			((Ageable)target).setAgeLock(true);
		case SILVERFISH:
		case SNOWMAN:
		case CREEPER:
		case SQUID:
		case BAT:
			if (target instanceof Creature)
				((Creature) target).setTarget(alf.getPlayer());
			
			EntityLiving eL = ((CraftLivingEntity)target).getHandle();
			//Clear the PathEntity (PathEntity pe = null)
			eL.getNavigation().g();
			
			Pet pet = new Pet(this.plugin, target, alf.getPlayer().getName() + "'s Pet", alf);
			pet.addEffect(new PetEffect(this));
			alf.setPet(pet);
			plugin.getCharacterManager().addPet(pet);
			
			alf.addEffect(new FollowEffect(this, 250L));
			
			return SkillResult.NORMAL;
		default:
			break;
		}

		return SkillResult.INVALID_TARGET;
	}

}
