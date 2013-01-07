package com.alf.skill.skills;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.skill.SkillType;
import com.alf.skill.TargetedSkill;

public class SkillAirBlast extends TargetedSkill {

	public SkillAirBlast(AlfCore plugin) {
		super(plugin, "AirBlast");
		setDescription("You blast back your target.");
		setUsage("/skill airblast");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill airblast" });
		setTypes(new SkillType[] { SkillType.FORCE, SkillType.COUNTER });
		setUseText("%alf% uses %skill%!");
	}
	
	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}

	public SkillResult use(Alf alf, LivingEntity target, String[] args) {
		if (target instanceof Player && ((Player)target).getName().equals(alf.getName()))
			return SkillResult.INVALID_TARGET;
		
		Location tLoc = target.getLocation();
		Location aLoc = alf.getPlayer().getLocation();
		
		double xDir = tLoc.getX() - aLoc.getX();
		double zDir = tLoc.getZ() - aLoc.getZ();
		
		target.setVelocity(new Vector(xDir / 3, 0.5D, zDir / 3));
		
		CharacterTemplate character = this.plugin.getCharacterManager().getCharacter(target);
		if (character.hasEffect("Combust"))
			character.removeEffect(character.getEffect("Combust"));
		
		return SkillResult.NORMAL;
	}
	
}
