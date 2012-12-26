package com.alf.skill;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.alf.AlfCore;
import com.alf.api.event.AlfChangeLevelEvent;
import com.alf.api.event.ClassChangeEvent;
import com.alf.chararacter.Alf;
import com.alf.chararacter.effect.Effect;
import com.alf.chararacter.effect.EffectType;
import com.alf.util.Messaging;
import com.alf.util.Setting;

public abstract class PassiveSkill extends Skill {

	private String applyText = null;
	private String unapplyText = null;
	private EffectType[] effectTypes = null;
	
	/**
	 * Constructs the outsourced skill.
	 * @param plugin
	 * @param name
	 */
	public PassiveSkill(AlfCore plugin, String name) {
		super(plugin, name);
		setUsage("Passive Skill");
		Bukkit.getServer().getPluginManager().registerEvents(new SkillListener(), plugin);
	}
	
	/**
	 * A passive skill can not be executed.
	 */
	public boolean execute(CommandSender sender, String identifier, String[] args) {
		Messaging.send(sender, "$1 is a passive skill and cannot be used!", new Object[] {
				getName() });
		return true;
	}

	/**
	 * Get the default configuration section for this skill.
	 */
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection section = super.getDefaultConfig();
		section.set(Setting.APPLY_TEXT.node(), "%alf% gained %skill%!");
		section.set(Setting.UNAPPLY_TEXT.node(), "%alf% lost %skill%!");
		return section;
	}
	
	/**
	 * Initiate the outsourced skill.
	 */
	public void init() {
		this.applyText = SkillConfigManager.getRaw(this, Setting.APPLY_TEXT, "");
		this.applyText = this.applyText.replace("%alf%", "$1").replace("%skill%", "$2");
		this.unapplyText = SkillConfigManager.getRaw(this, Setting.UNAPPLY_TEXT, "");
		this.unapplyText = this.unapplyText.replace("%alf%", "$1").replace("%skill%", "$2");
	}
	
	/**
	 * Set the effect types of this skill.
	 * @param effectTypes
	 */
	public void setEffectTypes(EffectType[] effectTypes) {
		this.effectTypes = effectTypes;
	}
	
	/**
	 * Try applying this effect to the Alf.
	 * @param alf
	 */
	public void tryApplying(Alf alf) {
		if (alf.hasAccessToSkill(this))
			if (alf.canUseSkill(this)) {
				if (! alf.hasEffect(getName()))
					apply(alf);
			} else unapply(alf);
	}
	
	/**
	 * Apply this effect to a given alf.
	 * @param alf
	 */
	protected void apply(Alf alf) {
		Effect effect = new Effect(this, getName(), this.effectTypes);
		effect.setPersistent(true);
		alf.addEffect(effect);
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), this.applyText, new Object[] {
			player.getDisplayName(), getName()
		});
	}

	/**
	 * Unapply this effect from a given alf.
	 * @param alf
	 */
	protected void unapply(Alf alf) {
		if (alf.hasEffect(getName())) {
			alf.removeEffect(alf.getEffect(getName()));
			Player player = alf.getPlayer();
			broadcast(player.getLocation(), this.unapplyText, new Object[] {
				player.getDisplayName(), getName()
			});
		}
	}
	
	/**
	 * Listens to class/level changes and handles checking for player compatibility.
	 * @author Eteocles
	 */
	public class SkillListener implements Listener {
		public SkillListener() {}
		
		@EventHandler(priority = EventPriority.MONITOR)
		public void onClassChange(ClassChangeEvent event) {
			PassiveSkill.this.tryApplying(event.getAlf());
		}
		
		@EventHandler(priority = EventPriority.MONITOR)
		public void onAlfChangeLevel(AlfChangeLevelEvent event) {
			PassiveSkill.this.tryApplying(event.getAlf());
		}
	}
	
}
