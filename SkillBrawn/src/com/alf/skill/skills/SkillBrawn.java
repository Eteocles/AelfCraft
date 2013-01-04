package com.alf.skill.skills;

import org.bukkit.configuration.ConfigurationSection;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.effect.common.DamageBuffEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Setting;

/**
 * A skill that ups attack by 10% for 8 seconds.
 * @author Eteocles
 */
public class SkillBrawn extends ActiveSkill {

	private final AlfCore plugin;
	
	public SkillBrawn(AlfCore plugin) {
		super(plugin, "Brawn");
		setDescription("You buff yourself for a $1 increase in dealt damage for $2 seconds.");
		setUsage("/skill brawn");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill brawn" });
		setTypes(new SkillType[] { SkillType.BUFF });
		setUseText("%alf% uses %skill%!");
		this.plugin = plugin;
	}
	
	/**
	 * Get the default config.
	 */
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection section = super.getDefaultConfig();
		section.set("duration", 8000);
		section.set("damage-percentage", 0.2D);
		return section;
	}
	
	/**
	 * Use the skill.
	 */
	@Override
	public SkillResult use(Alf alf, String[] args) {
		double damageBuff = SkillConfigManager.getUseSetting(alf, this, "damage-percentage", 0.1D, false);
		int buffDuration = SkillConfigManager.getUseSetting(alf, this, Setting.DURATION, 8000, false);
		
		alf.addEffect(new DamageBuffEffect(this, "Brawn Buff", buffDuration, damageBuff, 
				"$1 focuses and attains a temporary buffed state!",
				"$1 has lost his brawn!",
				plugin.getDamageManager()
				));
		
		return SkillResult.NORMAL;
	}

	/**
	 * Get the description.
	 */
	@Override
	public String getDescription(Alf a) {
		double damageBuff = SkillConfigManager.getUseSetting(a, this, "damage-percentage", 0.1D, false);
		int buffDuration = SkillConfigManager.getUseSetting(a, this, Setting.DURATION, 8000, false);
		return getDescription().replace("$1", damageBuff+"").replace("$2", buffDuration+"");
	}

}
