package com.alf.skill.skills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.PeriodicHealEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Setting;

/**
 * Heals the player using the skill.
 * @author Eteocles
 */
public class SkillBandage extends ActiveSkill {

	/**
	 * Construct the skill.
	 * @param plugin
	 */
	public SkillBandage(AlfCore plugin) {
		super(plugin, "Bandage");
		setDescription("You heal yourself by a small amount.");
		setUsage("/skill bandage");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill bandage" });
		setTypes(new SkillType[] { SkillType.HEAL });
		setUseText("%alf% uses %skill%!");
	}
	
	/**
	 * Get the default configuration.
	 */
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection config = super.getDefaultConfig();
		
		config.set(Setting.HEALTH_INCREASE.node(), 0.1D);
		config.set(Setting.DURATION.node(), 5000);
		config.set(Setting.DURATION_INCREASE.node(), 100);
		
		return config;
	}
	
	/**
	 * Use the skill.
	 */
	@Override
	public SkillResult use(Alf alf, String[] args) {
		double healPercent = SkillConfigManager.getUseSetting(alf, this, Setting.HEALTH_INCREASE, 0.1D, false);
		int duration = SkillConfigManager.getUseSetting(alf, this, Setting.DURATION, 3000, false);
		int durInc = SkillConfigManager.getUseSetting(alf, this, Setting.DURATION_INCREASE, 200, false);
		
		alf.addEffect(new BandageEffect(this, "Bandage", 1000L, (long)(duration + durInc), (int)(healPercent*alf.getMaxHealth()), 
				alf.getPlayer()));
		
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}
	
	/**
	 * The periodic healing effect induced by this skill.
	 * @author Eteocles
	 */
	public class BandageEffect extends PeriodicHealEffect {
		
		public BandageEffect(Skill skill, String name, long period, long duration, int tickHealth, Player applier) {
			super(skill, name, period, duration, tickHealth, applier);
			this.types.add(EffectType.SLOW);
		}
		
	}

}
