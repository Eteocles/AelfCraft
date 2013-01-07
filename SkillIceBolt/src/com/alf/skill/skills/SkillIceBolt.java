package com.alf.skill.skills;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.character.Pet;
import com.alf.character.effect.common.SlowEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Setting;

public class SkillIceBolt extends ActiveSkill {

	private Map<Snowball, Long> snowballs = new LinkedHashMap<Snowball, Long>(100) {

		private static final long serialVersionUID = -2058335978273226274L;

		protected boolean removeEldestEntry(Map.Entry<Snowball, Long> eldest) {
			return (size() > 60 || eldest.getValue() + 5000L <= System.currentTimeMillis() );
		}
	};

	public SkillIceBolt(AlfCore plugin) {
		super(plugin, "IceBolt");
		setDescription("You hurl a freezing orb of ice!");
		setUsage("/skill icebolt");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill icebolt" });
		setTypes(new SkillType[] { SkillType.ICE, SkillType.DAMAGING });
		setUseText("%alf% uses %skill%!");

		Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
	}

	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection config = super.getDefaultConfig();

		config.set(Setting.DAMAGE.node(), 20);
		config.set(Setting.DAMAGE_INCREASE.node(), 0.05D);
		config.set("velocity-mult", 6.0D);
		config.set("slow-duration", 3000);
		return config;
	}

	@Override
	public SkillResult use(Alf alf, String[] args) {
		Player player = alf.getPlayer();

		Snowball snowball = player.launchProjectile(Snowball.class);
		double mult = SkillConfigManager.getUseSetting(alf, this, "velocity-mult", 1.5D, false);
		snowball.setVelocity(snowball.getVelocity().multiply(mult));
		this.snowballs.put(snowball, System.currentTimeMillis());

		if (Math.random() < 0.15D) {
			Snowball snowball2 = player.launchProjectile(Snowball.class);
			snowball2.setVelocity(snowball2.getVelocity().multiply(mult));
			this.snowballs.put(snowball2, System.currentTimeMillis());
		}

		broadcastExecuteText(alf);
		return SkillResult.NORMAL;
	}

	@Override
	public String getDescription(Alf a) {
		return getDescription();
	}

	public class SkillEntityListener implements Listener {
		private final Skill skill;

		public SkillEntityListener(Skill skill) {
			this.skill = skill;
		}

		@EventHandler
		public void onEntityDamage(EntityDamageEvent event) {
			if (event.isCancelled() || ! (event instanceof EntityDamageByEntityEvent) || 
					! (event.getEntity() instanceof LivingEntity))
				return;
			EntityDamageByEntityEvent subevent = (EntityDamageByEntityEvent)event;
			Entity projectile = subevent.getDamager();

			if (! (projectile instanceof Snowball) || ! SkillIceBolt.this.snowballs.containsKey(projectile))
				return;

			SkillIceBolt.this.snowballs.remove(projectile);
			LivingEntity entity = (LivingEntity)subevent.getEntity();
			Entity dmger = ((Snowball)projectile).getShooter();

			if (dmger instanceof Player) {

				Alf alf = SkillIceBolt.this.plugin.getCharacterManager().getAlf((Player)dmger);

				if (! Skill.damageCheck((Player)dmger, entity)) {
					event.setCancelled(true);
					return;
				}

				CharacterTemplate chara = SkillIceBolt.this.plugin.getCharacterManager().getCharacter(entity);
				if (chara instanceof Pet || (chara instanceof Alf && ((Alf)chara).hasParty() && ((Alf)chara).getParty().isPartyMember(alf)))
					return;

				chara.addEffect(new SlowEffect(this.skill, "IceBolt", 
						(long)SkillConfigManager.getUseSetting(alf, this.skill, "slow-duration", 3000L, false),
						2, false, "$1 has been slowed by $2!", "$1 is no longer slowed!", alf));

				SkillIceBolt.this.addSpellTarget(entity, alf);
				int damage = SkillConfigManager.getUseSetting(alf, this.skill, Setting.DAMAGE, 20, false);
				damage += (int) SkillConfigManager.getUseSetting(alf, this.skill, Setting.DAMAGE_INCREASE, 0.05D, false);
				Skill.damageEntity(entity, alf.getPlayer(), damage, DamageCause.MAGIC, false);
				event.setCancelled(true);
			}
		}
	}

}
