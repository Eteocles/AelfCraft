package com.alf.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;

import com.alf.AlfCore;
import com.alf.api.event.AlfChangeLevelEvent;
import com.alf.api.event.ClassChangeEvent;
import com.alf.character.Alf;
import com.alf.util.Setting;

/**
 * Describes an outsourced skill.
 * @author Eteocles
 */
public class OutsourcedSkill extends Skill {

	private String[] permissions;
	private Permission permission;

	/**
	 * Construct the Outsourced Skill.
	 * @param plugin
	 * @param name
	 */
	public OutsourcedSkill(AlfCore plugin, String name) {
		super(plugin, name, null);
		Bukkit.getServer().getPluginManager().registerEvents(new SkillAlfListener(), plugin);
	}
	
	/**
	 * Set the permissions for this outsourced skill.
	 * @param permissions
	 */
	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
		this.permission = this.plugin.getServer().getPluginManager().getPermission(getName());
		if (this.permission != null)
			this.plugin.getServer().getPluginManager().removePermission(this.permission);
		Map<String, Boolean> children = new HashMap<String, Boolean>();
		for (String s : permissions)
			children.put(s, true);
		this.permission = new Permission(getName(), "Permission-Skill " + getName(), children);
		this.plugin.getServer().getPluginManager().addPermission(this.permission);
	}
	
	/**
	 * Try teaching the skill to the Alf.
	 * @param alf
	 */
	public void tryLearningSkill(Alf alf) {
		//If the skill exists for the player...
		if (alf.getAlfClass().hasSkill(getName()) || (alf.getSecondClass() != null) &&
				alf.getSecondClass().hasSkill(getName())) {
			//If the alf has a high enough skill level...
			if (alf.getSkillLevel(this) >= SkillConfigManager.getUseSetting(alf, this, Setting.LEVEL, 
					1, true)) {
				alf.addPermission(this.permission.getName());
			} else {
				alf.removePermission(this.permission.getName());
			}
		} else {
			if (this.permissions == null) {
				AlfCore.log(Level.SEVERE, "No permissions detected for skill: " + getName() + 
						"! Fix your config.");
				return;
			}
			alf.removePermission(this.permission.getName());
		}
	}
	
	/**
	 * Get the description for the skill.
	 */
	@Override
	public String getDescription(Alf alf) {
		return getDescription();
	}
	
	/**
	 * Does not handle this way.
	 */
	@Override
	public boolean execute(CommandSender cs, String command, String[] args) {
		return true;
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	public void init() {}
	
	/**
	 * Listens to Alf class/xp events and handles them.
	 * @author Eteocles
	 */
	public class SkillAlfListener implements Listener {
		public SkillAlfListener() {}
		
		@EventHandler(priority = EventPriority.MONITOR)
		public void onClassChange(final ClassChangeEvent event) {
			if (! event.isCancelled()) {
				OutsourcedSkill.this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(OutsourcedSkill.this.plugin, new Runnable() {
					public void run() {
						OutsourcedSkill.this.tryLearningSkill(event.getAlf());
					}
				} , 1L);
			}
		}
		
		@EventHandler(priority = EventPriority.MONITOR)
		public void onAlfChangeLevel(final AlfChangeLevelEvent event) {
			OutsourcedSkill.this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(OutsourcedSkill.this.plugin, new Runnable() {
				public void run() {
					OutsourcedSkill.this.tryLearningSkill(event.getAlf());
				}
			}, 1L);
		}
	}
	
}
