package com.alf.skill.skills;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.character.Monster;
import com.alf.character.Pet;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.PeriodicDamageEffect;
import com.alf.character.effect.common.StunEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Messaging;
import com.alf.util.Setting;

/**
 * A slight charge to a nearby enemy to launch an attack.
 * @author Eteocles
 *
 */
public class SkillPummel extends ActiveSkill {

	private Set<String> usingPlayers = new HashSet<String>();

	public SkillPummel(AlfCore plugin) {
		super(plugin, "Pummel");
		setDescription("You pummel a nearby player into the ground, with a small chance of bleeding, stunning, or taking health from your enemy!");
		setUsage("/skill pummel");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill pummel" });
		setTypes(new SkillType[] {SkillType.TEXT});
		Bukkit.getServer().getPluginManager().registerEvents(new SkillDamageListener(this), plugin);
	}

	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection section = super.getDefaultConfig();
		section.set("stun-duration", 1500);
		section.set("bleed-duration", 5000);
		section.set("health-stolen", 0.25);
		section.set(Setting.DAMAGE.node(), 1.5);
		return section;
	}

	/**
	 * Use the skill.
	 */
	@Override
	public SkillResult use(Alf alf, String[] args) {
		this.usingPlayers.add(alf.getName());

		broadcastExecuteText(alf);
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}

	public class SkillDamageListener implements Listener {

		private final Skill skill;

		public SkillDamageListener(Skill skill) {
			this.skill = skill;
		}

		@EventHandler(priority = EventPriority.LOWEST)
		public void onEntityDamage(EntityDamageByEntityEvent event) {
			if (! event.getCause().equals(DamageCause.ENTITY_ATTACK) || ! (event.getDamager() instanceof Player) ||
					! SkillPummel.this.usingPlayers.contains( ((Player)event.getDamager()).getName()))
				return;

			Player player = (Player) event.getDamager();
			Alf alf = SkillPummel.this.plugin.getCharacterManager().getAlf(player);
			SkillPummel.this.usingPlayers.remove(player.getName());

			event.setDamage(0);
			event.setCancelled(true);

			int damage = SkillPummel.this.plugin.getDamageManager().getItemDamage(player.getItemInHand().getType(), player);
			double damageMult = SkillConfigManager.getUseSetting(alf, this.skill, Setting.DAMAGE.node(), 1.5, false);
			int stunDuration = SkillConfigManager.getUseSetting(alf, this.skill, "stun-duration", 1500, false);
			int bleedDuration = SkillConfigManager.getUseSetting(alf, this.skill, "bleed-duration", 5000, false);
			double healthStolen = SkillConfigManager.getUseSetting(alf, this.skill, "health-stolen", 0.25, false);
			
			damage *= damageMult;

			if (event.getEntity() instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) event.getEntity();
				CharacterTemplate character = SkillPummel.this.plugin.getCharacterManager().getCharacter(entity);
				if (! (character instanceof Pet)) {
					if (character instanceof Alf)
						if (((Alf)character).hasParty() && (((Alf)character).getParty().isPartyMember(player)))
							return;
					if (Math.random() < 0.15D)
						character.addEffect(new StunEffect(this.skill, stunDuration));
					if (Math.random() < 0.15D) {
						alf.setHealth((int)(alf.getHealth() + damage*healthStolen));
						Messaging.send(player, "You have stolen $1 health from your enemy!", new Object[] { (int)(damage*healthStolen) });
					}
					if (Math.random() < 0.25D)
						character.addEffect(new BleedSkillEffect(this.skill, bleedDuration, 1000, 10, player));
					if (damage > 0) {
						SkillPummel.this.addSpellTarget(entity, alf);
						Skill.damageEntity(entity, player, damage, DamageCause.ENTITY_ATTACK, true);
					}
				}
			}
		}

	}
	
	public class BleedSkillEffect extends PeriodicDamageEffect
	  {
	    public BleedSkillEffect(Skill skill, long duration, long period, int tickDamage, Player applier)
	    {
	      super(skill, "Bleed", period, duration, tickDamage, applier);
	      this.types.add(EffectType.BLEED);
	    }

	    public void applyToMonster(Monster monster)
	    {
	      super.applyToMonster(monster);
	    }

	    public void applyToAlf(Alf alf)
	    {
	      super.applyToAlf(alf);
	      Player player = alf.getPlayer();
	      broadcast(player.getLocation(), "$1 is now bleeding!", new Object[] { player.getDisplayName() });
	    }

	    public void removeFromMonster(Monster monster)
	    {
	      super.removeFromMonster(monster);
	    }

	    public void removeFromAlf(Alf alf)
	    {
	      super.removeFromAlf(alf);
	      Player player = alf.getPlayer();
	      broadcast(player.getLocation(), "$1 is no longer bleeding!", new Object[] { player.getDisplayName() });
	    }
	  }

}
