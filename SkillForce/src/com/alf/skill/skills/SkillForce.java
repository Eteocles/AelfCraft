package com.alf.skill.skills;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.character.effect.common.SilenceEffect;
import com.alf.character.effect.common.StunEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Setting;

/**
 * A force skill that knockbacks the enemy and applies certain effects on chance.
 * @author Eteocles
 */
public class SkillForce extends ActiveSkill {

	public SkillForce(AlfCore plugin) {
		super(plugin, "Force");
		setDescription("You exert your force of presence on a target, knocking it back with a chance of stun and silence.");
		setUsage("/skill force");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill force" });
		setTypes(new SkillType[] { SkillType.INTERRUPT, SkillType.FORCE });
	}

	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection section = super.getDefaultConfig();
		section.set("stun-duration", 2000);
		section.set("silence-duration", 2000);
		section.set("effect-chance", 0.08D);
		section.set(Setting.RADIUS.node(), 2);
		return section;
	}

	/**
	 * Use the skill on a given target.
	 */
	@Override
	public SkillResult use(Alf alf, String[] args) {

		Player player  = alf.getPlayer();

		int stunDur = SkillConfigManager.getUseSetting(alf, this, "stun-duration", 2000, false);
		int silDur = SkillConfigManager.getUseSetting(alf, this, "silence-duration", 2000, false);
		double effChance = SkillConfigManager.getUseSetting(alf, this, "effect-chance", 0.15D, false);
		int radius = SkillConfigManager.getUseSetting(alf, this, Setting.RADIUS, 2, false);

		Location playerLoc = player.getLocation();
		
		for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
			if (e instanceof LivingEntity) {
				Location targetLoc = e.getLocation();

				double xDir = targetLoc.getX() - playerLoc.getX();
				double zDir = targetLoc.getZ() - playerLoc.getZ();
				Vector v = new Vector(xDir, 0.5D, zDir);
				e.setVelocity(v);

				CharacterTemplate chara = this.plugin.getCharacterManager().getCharacter((LivingEntity)e);

				if (Math.random() < effChance) {
					chara.addEffect(new StunEffect(this, stunDur));
				}

				if (Math.random() < effChance) {
					chara.addEffect(new SilenceEffect(this, silDur));
				}
			}
		}

		this.broadcastExecuteText(alf);

		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}

}