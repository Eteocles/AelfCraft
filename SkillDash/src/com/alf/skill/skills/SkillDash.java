package com.alf.skill.skills;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.skill.ActiveSkill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Messaging;
import com.alf.util.Util;

/**
 * A skill that causes a player to dash forward.
 * @author Eteocles
 */
public class SkillDash extends ActiveSkill {

	/**
	 * Construct the skill.
	 * @param plugin
	 */
	public SkillDash(AlfCore plugin) {
		super(plugin, "Dash");
		setDescription("You dash forward to evade and weave through your enemies!");
		setUsage("/skill dash");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill dash" });
		setTypes(new SkillType[] { SkillType.MOVEMENT });
		setUseText("%alf% uses %skill%");
	}

	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection section = super.getDefaultConfig();
		section.set("velocity", 1.5D);
		section.set("velocity-by-level", 0.1D);
		section.set("velocity-max", 3.0D);
		return section;
	}

	/**
	 * Use the skill.
	 */
	@Override
	public SkillResult use(Alf alf, String[] args) {
		Player player = alf.getPlayer();
		
		double magnitude = SkillConfigManager.getUseSetting(alf, this, "velocity", 1.5D, false);
		double levelMult = SkillConfigManager.getUseSetting(alf, this, "velocity-by-level", 0.1D, false);
		
		magnitude += levelMult*alf.getSkillLevel(this);
		
		double yaw = player.getEyeLocation().getYaw();
		double pitch = player.getEyeLocation().getPitch();
		double[] vector = Util.toCartesian(yaw, pitch, (int)magnitude);
		Vector v = new Vector(vector[0], 0, vector[2]);
		
		if (Util.isWayBlocked(player.getWorld(), player.getEyeLocation(), player.getEyeLocation().add(v), 0, 4) || 
				Util.isWayBlocked(player.getWorld(), player.getLocation(), player.getLocation().add(v), 0, 4)) {
			Messaging.send(player, "You can not use that skill as the way is blocked!", new Object[0]);
			return SkillResult.FAIL;
		}
		
		player.setVelocity(v);
		broadcastExecuteText(alf);
		
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}
	
	
	
}
