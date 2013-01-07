package com.alf.skill.skills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.effect.common.DisarmEffect;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.skill.TargetedSkill;
import com.alf.util.Setting;

public class SkillDisarm extends TargetedSkill {

	public SkillDisarm(AlfCore plugin) {
		super(plugin, "Disarm");
		setDescription("You disarm your enemy for a period of time!");
		setUsage("/skill disarm");
		setArgumentRange(0,0);
		setIdentifiers(new String[] {"skill disarm" });
		setTypes(new SkillType[] { SkillType.HARMFUL });
	}
	
	/**
	 * Get default config.
	 */
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection section = super.getDefaultConfig();
		section.set(Setting.DURATION.node(), 3000L);
		return section;
	}
	
	/**
	 * Use the skill.
	 */
	@Override
	public SkillResult use(Alf alf, LivingEntity target, String[] args) {
		if (target instanceof Player) {
			Alf tAlf = this.plugin.getCharacterManager().getAlf((Player)target);
			long duration = SkillConfigManager.getUseSetting(alf, this, Setting.DURATION, 3000, false);
			tAlf.addEffect(new DisarmEffect(this, duration, 
					"$1 was disarmed!", "$2 is no longer disarmed!"
			));
			broadcastExecuteText(alf, target);
			return SkillResult.NORMAL;
		}
		return SkillResult.INVALID_TARGET;
	}

	/**
	 * Get the description of the skill.
	 */
	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}

}
