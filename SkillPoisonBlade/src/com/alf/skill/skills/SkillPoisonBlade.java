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
import com.alf.skill.ActiveSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Messaging;
import com.alf.util.Setting;

public class SkillPoisonBlade extends ActiveSkill {

	private Set<String> usingPlayers = new HashSet<String>();

	public SkillPoisonBlade(AlfCore plugin) {
		super(plugin, "PoisonBlade");
		setDescription("You douse your blade with deadly poison, affecting the next enemy you strike.");
		setUsage("/skill poisonblade");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill poisonblade" });
		setTypes(new SkillType[] { SkillType.HARMFUL, SkillType.DAMAGING });
		Bukkit.getServer().getPluginManager().registerEvents(new SkillDamageListener(this), plugin);
	}
	
	@Override
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection section = super.getDefaultConfig();
		section.set("poison-damage", 15);
		section.set(Setting.PERIOD.node(), 2000);
		section.set(Setting.DURATION.node(), 10000);
		section.set(Setting.DURATION_INCREASE.node(), 1);
		
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

	/**
	 * Listen to skill damage events.
	 * @author Eteocles
	 */
	public class SkillDamageListener implements Listener {

		private final Skill skill;

		/**
		 * Construct the listener.
		 * @param skill
		 */
		public SkillDamageListener(Skill skill) {
			this.skill = skill;
		}

		@EventHandler(priority = EventPriority.LOWEST)
		public void onEntityDamage(EntityDamageByEntityEvent event) {
			if (! event.getCause().equals(DamageCause.ENTITY_ATTACK) || ! (event.getDamager() instanceof Player) ||
					! SkillPoisonBlade.this.usingPlayers.contains( ((Player)event.getDamager()).getName()))
				return;

			Player att = (Player) event.getDamager();
			Alf alf = SkillPoisonBlade.this.plugin.getCharacterManager().getAlf(att);
			SkillPoisonBlade.this.usingPlayers.remove(alf.getName());

			event.setDamage(0);
			event.setCancelled(true);

			int damage = SkillPoisonBlade.this.plugin.getDamageManager().getItemDamage(
					att.getItemInHand().getType(), att);
			int poisonDamage = SkillConfigManager.getUseSetting(alf, this.skill, "poison-damage", 15, false);
			int tickPeriod = SkillConfigManager.getUseSetting(alf, this.skill, Setting.PERIOD, 2000, false);
			int duration = SkillConfigManager.getUseSetting(alf, this.skill, Setting.DURATION, 10000, false);
			int durationInc = SkillConfigManager.getUseSetting(alf, this.skill, Setting.DURATION_INCREASE, 1, false);
			
			if (event.getEntity() instanceof LivingEntity) {
				CharacterTemplate def = SkillPoisonBlade.this.plugin.getCharacterManager().getCharacter((LivingEntity)event.getEntity());
				if (! (def instanceof Pet)) {
					if (def.hasEffect("Poison2"))
						return;
					if (def.hasEffect("Poison1"))
						def.addEffect(new PoisonSkillEffect(this.skill, 2, (long)tickPeriod, (long)(duration + (durationInc * alf.getSkillLevel(this.skill))), 2*poisonDamage, att));
					else
						def.addEffect(new PoisonSkillEffect(this.skill, 1, (long)tickPeriod, (long)(duration + (durationInc * alf.getSkillLevel(this.skill))), poisonDamage, att));
				}
				Skill.damageEntity((LivingEntity) event.getEntity(), att, damage, DamageCause.ENTITY_ATTACK, true);
			}
		}

	}

	/**
	 * A periodic damaging poison effect.
	 * @author Eteocles
	 *
	 */
	public class PoisonSkillEffect extends PeriodicDamageEffect
	{
		public PoisonSkillEffect(Skill skill, int tier, long period, long duration, int tickDamage, Player applier) {
			super(skill, "Poison"+tier, period, duration, tickDamage, applier);
			this.types.add(EffectType.POISON);
			addMobEffect(19, (int)(duration / 1000L) * 20, 0, true);
		}

		public void applyToMonster(Monster monster) {
			super.applyToMonster(monster);
		}

		public void applyToAlf(Alf alf) {
			super.applyToAlf(alf);
		}

		public void removeFromMonster(Monster monster) {
			super.removeFromMonster(monster);
			broadcast(monster.getEntity().getLocation(), "$1 has recovered from the poison!", 
					new Object[] { Messaging.getLivingEntityName(monster) });
		}

		public void removeFromAlf(Alf alf) {
			super.removeFromAlf(alf);
			Player player = alf.getPlayer();
			broadcast(player.getLocation(), "$1 has recovered from the poison!", new Object[] { player.getDisplayName() });
		}
	}
}
