package com.alf.skill.skills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.ExpirableEffect;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.skill.TargetedSkill;
import com.alf.util.Setting;

public class SkillBlind extends TargetedSkill {

	private String applyText = "$1 has blinded $2 with $3!";
	private String expireText = "$1 has recovered their vision!";

	public SkillBlind(AlfCore plugin) {
		super(plugin, "Blind");
		setDescription("You blind your enemy, disorienting them!");
		setUsage("/skill blind");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill blind" });
		setTypes(new SkillType[] { SkillType.HARMFUL, SkillType.DARK });
	}

	public ConfigurationSection getDefaultConfig() {

		ConfigurationSection section = super.getDefaultConfig();
		section.set(Setting.DURATION.node(), 3000);
		section.set(Setting.DURATION_INCREASE.node(), 0.5);
		return section;
	}
	
	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}

	public SkillResult use(Alf alf, LivingEntity target, String[] args) {
		if (target instanceof Player && alf.getName().equals(((Player)target).getName()))
				return SkillResult.INVALID_TARGET;
		CharacterTemplate character = this.plugin.getCharacterManager().getCharacter(target);
		int duration = SkillConfigManager.getUseSetting(alf, this, Setting.DURATION, 3, false);
		double durationInc = SkillConfigManager.getUseSetting(alf, this, Setting.DURATION_INCREASE, 0.5, false);
		duration += duration*durationInc;
		character.addEffect(new BlindEffect(this, (long)duration, alf.getPlayer()));
		this.broadcastExecuteText(alf, target);
		return SkillResult.NORMAL;
	}

	public class BlindEffect extends ExpirableEffect {
		private final Player player;

		public BlindEffect(Skill skill, long duration, Player player) {
			super(skill, "Blind", duration);
			this.player = player;
			addMobEffect(15, (int)(duration / 1000L * 20L), 3, false);
			this.types.add(EffectType.DARK);
			this.types.add(EffectType.BLIND);
		}

		public void applyToAlf(Alf alf) {
			super.applyToAlf(alf);
			broadcast(alf.getPlayer().getLocation(), SkillBlind.this.applyText, 
					new Object[] { this.player.getDisplayName(), alf.getPlayer().getDisplayName(),
				"Blind" });
		}

		public void removeFromAlf(Alf alf) {
			super.removeFromAlf(alf);
			broadcast(alf.getPlayer().getLocation(), SkillBlind.this.expireText, 
					new Object[] { alf.getPlayer().getDisplayName()});
		}

	}

}
