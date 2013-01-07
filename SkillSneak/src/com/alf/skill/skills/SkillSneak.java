package com.alf.skill.skills;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.character.Alf;
import com.alf.character.effect.common.SneakEffect;
import com.alf.skill.ActiveSkill;
import com.alf.skill.SkillConfigManager;
import com.alf.skill.SkillType;
import com.alf.util.Messaging;
import com.alf.util.Setting;

/**
 * A skill in which a player sneaks.
 * @author Eteocles
 */
public class SkillSneak extends ActiveSkill {

	private boolean damageCancels;
	private boolean attackCancels;

	/**
	 * Construct the skill.
	 * @param plugin
	 */
	public SkillSneak(AlfCore plugin) {
		super(plugin, "Sneak");
		setDescription("You crouch into the shadows.");
		setUsage("/skill stealth");
		setArgumentRange(0, 0);
		setIdentifiers(new String[] { "skill sneak" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.PHYSICAL, SkillType.STEALTHY });
		Bukkit.getServer().getPluginManager().registerEvents(new SkillEventListener(), plugin);
	}

	/**
	 * Get the default configuration.
	 */
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection node = super.getDefaultConfig();
		node.set(Setting.DURATION.node(), 600000);
		node.set("damage-cancels", true);
		node.set("attacking-cancels", true);
		node.set("refresh-interval", 5000);
		return node;
	}

	/**
	 * Initiate the skill.
	 */
	public void init() {
		super.init();
		this.damageCancels = SkillConfigManager.getRaw(this, "damage-cancels", true);
		this.attackCancels = SkillConfigManager.getRaw(this, "attacking-cancels", true);
	}

	/**
	 * Use the skill.
	 */
	public SkillResult use(Alf alf, String[] args) {
		if (alf.hasEffect("Sneak")) {
			alf.removeEffect(alf.getEffect("Sneak"));
		} else {
			Messaging.send(alf.getPlayer(), "You are now sneaking", new Object[0]);

			int duration = SkillConfigManager.getUseSetting(alf, this, Setting.DURATION, 600000, false);
			int period = SkillConfigManager.getUseSetting(alf, this, "refresh-interval", 5000, true);
			alf.addEffect(new SneakEffect(this, period, duration));
		}
		return SkillResult.NORMAL;
	}

	/**
	 * Get the description.
	 */
	public String getDescription(Alf alf) {
		return getDescription();
	}

	/**
	 * Listens to sneak/damage events.
	 * @author Eteocles
	 */
	public class SkillEventListener implements Listener {
		public SkillEventListener() {}

		@EventHandler(priority=EventPriority.MONITOR)
		public void onEntityDamage(EntityDamageEvent event) {
			if ((event.isCancelled()) || (!SkillSneak.this.damageCancels) || (event.getDamage() == 0)) {
				return;
			}
			
			Player player = null;
			if (event.getEntity() instanceof Player) {
				player = (Player)event.getEntity();
				Alf alf = SkillSneak.this.plugin.getCharacterManager().getAlf(player);
				if (alf.hasEffect("Sneak")) {
					player.setSneaking(false);
					alf.removeEffect(alf.getEffect("Sneak"));
				}
			}
			player = null;
			if ((SkillSneak.this.attackCancels) && ((event instanceof EntityDamageByEntityEvent))) {
				EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent)event;
				if (subEvent.getDamager() instanceof Player)
					player = (Player)subEvent.getDamager();
				else if ((subEvent.getDamager() instanceof Projectile) && 
						(((Projectile)subEvent.getDamager()).getShooter() instanceof Player)) {
					player = (Player)((Projectile)subEvent.getDamager()).getShooter();
				}

				if (player != null) {
					Alf alf = SkillSneak.this.plugin.getCharacterManager().getAlf(player);
					if (alf.hasEffect("Sneak")) {
						player.setSneaking(false);
						alf.removeEffect(alf.getEffect("Sneak"));
					}
				}
			}
		}

		@EventHandler(priority=EventPriority.HIGHEST)
		public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
			Alf alf = SkillSneak.this.plugin.getCharacterManager().getAlf(event.getPlayer());
			if (alf.hasEffect("Sneak")) {
				event.getPlayer().setSneaking(true);
				event.setCancelled(true);
			}
		}
	}

}
