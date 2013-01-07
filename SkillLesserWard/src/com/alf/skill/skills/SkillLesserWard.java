package com.alf.skill.skills;

import org.bukkit.configuration.ConfigurationSection;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.effect.common.WardEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Setting;

public class SkillLesserWard extends ActiveSkill {

	public SkillLesserWard(AlfCore plugin) {
		super(plugin, "LesserWard");
		setDescription("You cast a lesser ward to improve your armor rating!");
		setUsage("/skill lward");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill lward", "skill lesserward" });
		setTypes(new SkillType[] { SkillType.MOVEMENT });
		setUseText("%alf% uses %skill%");
	}

	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection section = super.getDefaultConfig();
		section.set(Setting.DURATION.node(), 15000);
		return section;
	}

	/**
	 * Use the skill.
	 */
	@Override
	public SkillResult use(Alf alf, String[] args) {
		int duration = SkillConfigManager.getUseSetting(alf, this, Setting.DURATION.node(), 15000, false);
		alf.addEffect(new WardEffect(this, "LesserWard", duration, 0.25D));
		broadcastExecuteText(alf);
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}
	
}
