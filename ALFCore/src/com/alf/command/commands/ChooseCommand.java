package com.alf.command.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.event.ClassChangeEvent;
import com.alf.character.Alf;
import com.alf.character.classes.AlfClass;
import com.alf.command.BasicInteractiveCommand;
import com.alf.command.BasicInteractiveCommandState;
import com.alf.command.CommandHandler;
import com.alf.command.InteractiveCommandState;
import com.alf.util.Messaging;
import com.alf.util.Properties;

public class ChooseCommand extends BasicInteractiveCommand {

	private AlfCore plugin;
	private Map<String, AlfClass> pendingClassSelections = new HashMap<String, AlfClass>();
	private Map<String, Double> pendingClassCostStatus = new HashMap<String, Double>();
	
	public ChooseCommand(AlfCore plugin) {
		super("Choose Class");
		this.plugin = plugin;
		setStates(new InteractiveCommandState[] { new StateA(), new StateB() });
		setDescription("Selects a class or specialization");
		setUsage("/alf choose §9<type>");
	}
	
	@Override
	public String getCancelIdentifier() {
		return "alf cancel";
	}

	/**
	 * Handle command cancelled.
	 */
	@Override
	public void onCommandCancelled(CommandSender sender) {
		if (! (sender instanceof Player))
			if (sender instanceof ConsoleCommandSender) {
				Messaging.send(sender, "You are not a player!", new Object[0]);
				return;
			}
		
		this.pendingClassSelections.remove(sender);
		this.pendingClassCostStatus.remove(sender);
	}

	class StateA extends BasicInteractiveCommandState {
		
		public StateA() {
			super(new String[] { "alf choose" });
			setArgumentRange(1, 1);
		}
		
		/**
		 * Execute the command state.
		 */
		public boolean execute(CommandSender sender, String identifier, String[] args) {
			if (! (sender instanceof Player))
				Messaging.send(sender, "You are not a player!", new Object[0]);
			
			Properties props = AlfCore.properties;
			Player player = (Player)sender;
			Alf alf = ChooseCommand.this.plugin.getCharacterManager().getAlf(player);
			AlfClass currentClass = alf.getAlfClass();
			AlfClass newClass = ChooseCommand.this.plugin.getClassManager().getClass(args[0]);
			
			if (newClass == null) {
				Messaging.send(player, "Class not found.", new Object[0]);
				return false;
			}
			
			if (newClass.equals(currentClass) || (newClass.equals(alf.getSecondClass()))) {
				Messaging.send(player, "You are already set as this Class.", new Object[0]);
				return false;
			}
			
			if (! newClass.isPrimary()) {
				Messaging.send(player, "That is not a primary Class!", new Object[0]);
				return false;
			}
			
			if (! newClass.hasNoParents()) {
				for (AlfClass parentClass : newClass.getStrongParents()) {
					if (! alf.isMaster(parentClass)) {
						Messaging.send(player, "$1 requires you to master: $2", new Object[] { newClass.getName(),
								newClass.getStrongParents().toString().replace("[", "").replace("]", "") }); 
						return false;
					}
				}
				
				boolean masteredOne = false;
				for (AlfClass parentClass : newClass.getWeakParents()) {
					if (alf.isMaster(parentClass)) {
						masteredOne = true;
						break;
					}
				}
				
				if (! masteredOne && ! newClass.getWeakParents().isEmpty()) {
					Messaging.send(player, "$1 requires you to master one of: $2", new Object[] {
						newClass.getName(), newClass.getWeakParents().toString().replace("[", "").replace("]", "")
					});
					return false;
				}
			}
			
			if (! newClass.isDefault() && ! CommandHandler.hasPermission(player, "alf.classes." + 
					newClass.getName().toLowerCase())) {
				Messaging.send(player, "You don't have permission for $1.", new Object[] { newClass.getName() });
				return false;
			}
			
			double cost = newClass.getCost();
			
			if (alf.getExperience(newClass) > 0.0D)
				cost = props.oldClassSwapCost;
			
			if (props.firstSwitchFree && currentClass.isDefault())
				cost = 0.0D;
			else if (alf.isMaster(newClass) && props.swapMasterFree)
				cost = 0.0D;
			
			if (props.economy && cost > 0.0D && ! AlfCore.econ.has(player.getName(), cost)) {
				Messaging.send(player, "It will cost $1 to switch classes,  you only have $2", new Object[] {
						AlfCore.econ.format(cost), AlfCore.econ.format(AlfCore.econ.getBalance(player.getName()))
				});
				return false;
			}
			
			ChooseCommand.this.pendingClassCostStatus.put(player.getName(), cost);
			
			Messaging.send(sender, "You have chosen...", new Object[0]);
			Messaging.send(sender, "$1: $2", new Object[] {
				newClass.getName(), newClass.getDescription().toLowerCase()	
			});
			
			String skills = "";
			for (String skillName : newClass.getSkillNames()) {
				if (skills.length() > 80) {
					Messaging.send(sender, "Skills:" + skills, new Object[0]);
					skills = "";
				} skills += skillName + " ";
			}
			
			if (! skills.isEmpty())
				Messaging.send(sender, "Skills: " + skills, new Object[0]);
			
			if (cost > 0.0D)
				Messaging.send(sender, "$1: $2",  new Object[] { "Fee", AlfCore.econ.format(cost) });
			
			Messaging.send(sender, "Please §8/alf confirm §7 or §8/alf cancel §7this selection.", new Object[0]);
			ChooseCommand.this.pendingClassSelections.put(player.getName(), newClass);
			
			return true;
		}
	}
	
	class StateB extends BasicInteractiveCommandState {
		
		/**
		 * Construct the state.
		 */
		public StateB() {
			super(new String[] {"alf confirm"});
			setArgumentRange(0,0);
		}
		
		/**
		 * Execute the command state.
		 */
		public boolean execute(CommandSender sender, String identifier, String[] args) {
			if (!(sender instanceof Player))
				return true;
			
			Player player = (Player) sender;
			Alf alf = ChooseCommand.this.plugin.getCharacterManager().getAlf(player);
			AlfClass currentClass = alf.getAlfClass();
			AlfClass newClass = (AlfClass)ChooseCommand.this.pendingClassSelections.remove(player.getName());
			double cost = ChooseCommand.this.pendingClassCostStatus.remove(player.getName());
			
			ClassChangeEvent event = new ClassChangeEvent(alf, currentClass, newClass, cost);
			Bukkit.getPluginManager().callEvent(event);
			
			if (event.isCancelled()) {
				ChooseCommand.this.cancelInteraction(player);
				Messaging.send(player, "You're not allowed to join that Class.", new Object[0]);
				return true;
			}
			
			cost = event.getCost();
			
			if (cost > 0.0D) {
				if (AlfCore.econ.has(player.getName(), cost)) {
					AlfCore.econ.withdrawPlayer(player.getName(), cost);
					Messaging.send(alf.getPlayer(), "The Gods are pleased with your offering of $1.",
							new Object[] { AlfCore.econ.format(cost) });
				} else {
					Messaging.send(alf.getPlayer(), "You are unable to meet the offering of $1 to become $2.",
							new Object[] { AlfCore.econ.format(cost), newClass.getName() });
					return true;
				}
			}
			
			alf.changeAlfClass(newClass, false);
			Messaging.send(player, "Welcome to the way of the $1!", new Object[] { newClass.getName() });
			
			ChooseCommand.this.plugin.getCharacterManager().saveAlf(alf, false);
			return true;
		}
		
	}
	
}
