package com.alf.skill.skills;

import net.minecraft.server.v1_4_6.EntityLiving;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.Pet;
import com.alf.character.effect.common.PetFollowEffect;
import com.alf.character.effect.common.PetEffect;
import com.alf.skill.SkillType;
import com.alf.skill.TargetedSkill;
import com.alf.util.FireworkEffectUtil;
import com.alf.util.Messaging;

public class SkillTame extends TargetedSkill {

	private AlfCore plugin;

	public SkillTame(AlfCore plugin) {
		super(plugin, "Tame");
		setDescription("You tame a mob and make it your pet!");
		setUsage("/skill tame");
		setArgumentRange(0,0);
		setIdentifiers(new String[] {"skill tame"});
		setTypes(new SkillType[] {SkillType.UNBINDABLE});
		setFireworkEffect(FireworkEffect.builder().flicker(true).withColor(Color.TEAL).withColor(Color.PURPLE).trail(true).with(Type.BURST).build());
		this.plugin = plugin;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}


	@Override
	public SkillResult use(Alf alf, LivingEntity target, String[] args) {

		if (alf.getPet() != null) { 
			Messaging.send(alf.getPlayer(), "You already have a loving pet. Why would you want to replace it?", new Object[0]);
			return SkillResult.FAIL;
		}

		if (plugin.getCharacterManager().getCharacter(target) instanceof Pet) {
			Messaging.send(alf.getPlayer(), "That " + getEntityName(target) + " is already someone's pet!", new Object[0]);
			return SkillResult.FAIL;
		}
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
		case VILLAGER:
		case WOLF:
			if (target instanceof Ageable) {
				((Ageable)target).setBaby();
				((Ageable)target).setAgeLock(true);
			}
		case SILVERFISH:
		case SNOWMAN:
		case CREEPER:
		case BAT:
		case SPIDER:
		case CAVE_SPIDER:
		case BOAT:
//		case SKELETON:
//		case SQUID:
			if (target instanceof Creature)
				((Creature) target).setTarget(alf.getPlayer());

			EntityLiving eL = ((CraftLivingEntity)target).getHandle();
			//Clear the PathEntity (PathEntity pe = null)
			eL.getNavigation().g();

			Pet pet = new Pet(this.plugin, target, alf.getPlayer().getName() + "'s Pet", alf);
			pet.addEffect(new PetEffect(this));
			alf.setPet(pet);
			plugin.getCharacterManager().addPet(pet);

			alf.addEffect(new PetFollowEffect(this, 250L));

			broadcastExecuteText(alf, target);
			Messaging.send(alf.getPlayer(), "You have a new pet! Congratulations!", new Object[0], ChatColor.AQUA);
			
			//Display firework effect :)
			try {
				FireworkEffectUtil.playFirework(target.getWorld(), target.getLocation(), getFireworkEffect());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return SkillResult.NORMAL;
		default:
			break;
		}

		return SkillResult.INVALID_TARGET;
	}

}
