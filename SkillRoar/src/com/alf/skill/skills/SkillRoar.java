package com.alf.skill.skills;

import java.util.List;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.CharacterManager;
import com.alf.character.Monster;
import com.alf.character.Pet;
import com.alf.skill.ActiveSkill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Messaging;
import com.alf.util.Setting;

/**
 * A skill that taunts nearby enemies (mobs) and forces them to target you.
 * @author Eteocles
 */
public class SkillRoar extends ActiveSkill {

	private final AlfCore plugin;
	
	public SkillRoar(AlfCore plugin) {
		super(plugin, "Roar");
		setDescription("You roar to taunt nearby enemies into targeting you.");
		setUsage("/skill roar");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill roar" });
		setTypes(new SkillType[] { SkillType.PHYSICAL });
		setUseText("%alf% uses %skill%!");
		
		this.plugin = plugin;
	}
	
	/**
	 * Get the default config.
	 */
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection section = super.getDefaultConfig();
		section.set(Setting.RADIUS.node(), 5);
		return section;
	}
	
	@Override
	public SkillResult use(Alf alf, String[] args) {
		
		Player player = alf.getPlayer();
		CharacterManager cm = plugin.getCharacterManager();
		
		int radius = SkillConfigManager.getUseSetting(alf, this, Setting.RADIUS.node(), 5, false);
		
		List<Entity> entities = player.getNearbyEntities(radius, radius, radius);
		
		if (entities.isEmpty()) {
			Messaging.send(player, "There are no presences around for you to taunt!", new Object[0]);
			return SkillResult.CANCELLED;
		}
		
		//Get surrounding entities.
		for (Entity e : entities) {
			if (e instanceof org.bukkit.entity.Monster) {
				Monster m = cm.getMonster((LivingEntity)e);
				if (! (m instanceof Pet))
					((org.bukkit.entity.Monster) e).setTarget(player);
			}
		}

		this.broadcastExecuteText(alf);
		
		player.getWorld().playSound(player.getLocation(), Sound.GHAST_SCREAM, 1, 0);
		Messaging.send(player, "Your loud roar startles your enemies and causes them to attack you!", new Object[0]);
		
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}

}
