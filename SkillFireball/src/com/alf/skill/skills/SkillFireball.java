package com.alf.skill.skills;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
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
import com.alf.character.effect.common.CombustEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Setting;

/**
 * A skill to shoot a fireball at your enemy.
 * @author Eteocles
 */
public class SkillFireball extends ActiveSkill {

	private Map<SmallFireball, Long> fireballs = new LinkedHashMap<SmallFireball, Long>(100) {
		
		private static final long serialVersionUID = -2058335978273226274L;

		protected boolean removeEldestEntry(Map.Entry<SmallFireball, Long> eldest) {
			return (size() > 60 || eldest.getValue() + 5000L <= System.currentTimeMillis() );
		}
	};
	
	public SkillFireball(AlfCore plugin) {
		super(plugin, "FireBall");
		setDescription("You hurl an orb of fire!");
		setUsage("/skill fireball");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill fireball" });
		setTypes(new SkillType[] { SkillType.FIRE, SkillType.DAMAGING });
		setUseText("%alf% uses %skill!%");
		
		Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
	}
	
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection config = super.getDefaultConfig();
		
		config.set(Setting.DAMAGE.node(), 20);
		config.set(Setting.DAMAGE_INCREASE.node(), 0.05D);
		config.set("velocity-mult", 6.0D);
		config.set("fire-ticks", 100);
		
		return config;
	}
	
	@Override
	public SkillResult use(Alf alf, String[] args) {
		Player player = alf.getPlayer();
		
		SmallFireball fireball = player.launchProjectile(SmallFireball.class);
		fireball.setFireTicks(SkillConfigManager.getUseSetting(alf, this, "fire-ticks", 100, false));
		double mult = SkillConfigManager.getUseSetting(alf, this, "velocity-mult", 1.5D, false);
		fireball.setVelocity(fireball.getVelocity().multiply(mult));
		this.fireballs.put(fireball, System.currentTimeMillis());

		if (Math.random() < 0.15D) {
			SmallFireball fireball2 = player.launchProjectile(SmallFireball.class);
			fireball2.setFireTicks(SkillConfigManager.getUseSetting(alf, this, "fire-ticks", 100, false));
			mult = SkillConfigManager.getUseSetting(alf, this, "velocity-mult", 3.0D, false);
			fireball2.setVelocity(fireball2.getVelocity().multiply(mult));
			this.fireballs.put(fireball2, System.currentTimeMillis());
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
			if (event.isCancelled() || ! (event instanceof EntityDamageByEntityEvent) || ! (event.getEntity() instanceof LivingEntity))
				return;
			EntityDamageByEntityEvent subevent = (EntityDamageByEntityEvent)event;
			Entity projectile = subevent.getDamager();
			
			if (! (projectile instanceof SmallFireball) || !SkillFireball.this.fireballs.containsKey(projectile))
				return;
			
			SkillFireball.this.fireballs.remove(projectile);
			LivingEntity entity = (LivingEntity) subevent.getEntity();
			Entity dmger = ((SmallFireball)projectile).getShooter();
			
			if (dmger instanceof Player) {
				Alf alf = SkillFireball.this.plugin.getCharacterManager().getAlf((Player)dmger);
				
				if (! Skill.damageCheck((Player)dmger, entity)) {
					event.setCancelled(true);
					return;
				}
				
				entity.setFireTicks(SkillConfigManager.getUseSetting(alf, this.skill, "fire-ticks", 100, false));
				CharacterTemplate chara = SkillFireball.this.plugin.getCharacterManager().getCharacter(entity);
				if (chara instanceof Pet || (chara instanceof Alf  && ((Alf)chara).hasParty() &&  ((Alf)chara).getParty().isPartyMember(alf)))
					return;
				chara.addEffect(new CombustEffect(this.skill, (Player)dmger));
				
				SkillFireball.this.addSpellTarget(entity, alf);
				int damage = SkillConfigManager.getUseSetting(alf, this.skill, Setting.DAMAGE, 20, false);
				damage += (int) SkillConfigManager.getUseSetting(alf, this.skill, Setting.DAMAGE_INCREASE, 0.05D, false);
				Skill.damageEntity(entity, alf.getPlayer(), damage, DamageCause.MAGIC, false);
				event.setCancelled(true);
			}
		}
	}
	
}
