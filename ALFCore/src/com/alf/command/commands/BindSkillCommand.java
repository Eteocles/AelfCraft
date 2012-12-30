package com.alf.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.command.BasicCommand;
import com.alf.skill.OutsourcedSkill;
import com.alf.skill.PassiveSkill;
import com.alf.skill.Skill;
import com.alf.skill.SkillType;
import com.alf.util.MaterialUtil;
import com.alf.util.Messaging;

/**
 * A command to bind a skill to a material.
 * @author Eteocles
 */
public class BindSkillCommand extends BasicCommand {

	private final AlfCore plugin;

	/**
	 * Construct the command.
	 * @param plugin
	 */
	public BindSkillCommand(AlfCore plugin) {
		super("BindSkill");
		this.plugin = plugin;
		setDescription("Binds a skill to an item");
		setUsage("/bind §9<skill> §8[args]");
		setArgumentRange(0, 1000);
		setIdentifiers(new String[] { "bind" });
		setPermission("alf.bind");
	}
	
	/**
	 * Execute the command.
	 */
	@Override
	public boolean execute(CommandSender sender, String msg, String[] args) {
		if (! (sender instanceof Player))
			return false;
		
		Player player = (Player) sender;
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		Material mat = player.getItemInHand().getType();
		
		if (! alf.canEquipItem(player.getInventory().getHeldItemSlot()))
			return false;
		
		if (args.length > 0) {
			Skill skill = this.plugin.getSkillManager().getSkill(args[0]);
			if (skill == null)
				skill = this.plugin.getSkillManager().getSkillFromIdent("skill " + args[0], sender);
			
			if (skill != null && alf.canUseSkill(skill.getName())) {
				if (skill instanceof PassiveSkill || skill instanceof OutsourcedSkill) {
					Messaging.send(player, "You can not bind passive skills!", new Object[0], ChatColor.RED);
					return false;
				}
				
				if (skill.isType(SkillType.UNBINDABLE)) {
					Messaging.send(player, "You can not bind that skill!", new Object[0], ChatColor.RED);
					return false;
				}
				
				if (mat == Material.AIR){
					Messaging.send(sender, "You must be holding an item to bind a skill!", new Object[0], ChatColor.RED);
					return false;
				}
				
				args[0] = skill.getName();
				
				alf.bind(mat, args);
				Messaging.send(sender,  "$1 has been bound to $2.", new Object[] { MaterialUtil.getFriendlyName(mat), skill.getName() });
			} else {
				Messaging.send(sender, "That skill does not exist for your class.", new Object[0]);
			}
		} else {
			alf.unbind(mat);
			Messaging.send(sender, "$1 is no longer bound to a skill.", new Object[] { MaterialUtil.getFriendlyName(mat) });
		}
		
		return true;
	}

}
