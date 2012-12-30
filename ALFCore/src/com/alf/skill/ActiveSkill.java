package com.alf.skill;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.alf.AlfCore;
import com.alf.api.SkillResult;
import com.alf.api.event.SkillCompleteEvent;
import com.alf.api.event.SkillUseEvent;
import com.alf.character.Alf;
import com.alf.character.CharacterManager;
import com.alf.character.classes.AlfClass;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.common.SlowEffect;
import com.alf.util.Messaging;
import com.alf.util.Setting;

/**
 * Describes a skill that runs actively.
 * @author Eteocles
 */
public abstract class ActiveSkill extends Skill {

	private String useText;
	private boolean awardExpOnCast = true;
	
	/**
	 * Constructs an ActiveSkill.
	 * @param plugin
	 * @param name
	 */
	public ActiveSkill(AlfCore plugin, String name) {
		super(plugin, name);
	}
	
	/**
	 * Get the default configuration for this skill.
	 */
	public ConfigurationSection getDefaultConfig() {
		ConfigurationSection section = super.getDefaultConfig();
		section.set(Setting.USE_TEXT.node(), "%alf% used %skill%!");
		return section;
	}
	
	/**
	 * Get the text displayed for this skill's usage.
	 * @return
	 */
	public String getUseText() {	
		return this.useText;	
	}

	/**
	 * Set the text displayed for this skill's usage.
	 * @param useText
	 */
	public void setUseText(String useText) {	
		this.useText = useText;	
	}
	
	protected boolean addDelayedSkill(Alf alf, int delay, String identifier, String[] args) {
		Player player = alf.getPlayer();
		DelayedSkill dSkill = new DelayedSkill(identifier, player, delay, this, args);
		broadcast(player.getLocation(), "$1 begins to use $2!", new Object[] { player.getDisplayName(), getName() });
		this.plugin.getCharacterManager().getDelayedSkills().put(alf, dSkill);
		alf.setDelayedSkill(dSkill);
		return true;
	}
	
	/**
	 * Initiate this active skill.
	 */
	public void init() {
		String useText = SkillConfigManager.getRaw(this, Setting.USE_TEXT, "%alf% used %skill%!");
		useText = useText.replace("%alf%", "$1").replace("%skill%", "$2");
		setUseText(useText);
	}
	
	/**
	 * Award experience to the Alf for using this skill.
	 * @param alf
	 */
	private void awardExp(Alf alf) {
		if (alf.canGain(AlfClass.ExperienceType.SKILL))
			alf.gainExp(SkillConfigManager.getUseSetting(alf, this, 
					Setting.EXP, 0, false), AlfClass.ExperienceType.SKILL,
					alf.getViewingLocation(1.0D));
	}
	
	/**
	 * Broadcast to nearby players of the Skill's execution.
	 * @param alf
	 */
	protected void broadcastExecuteText(Alf alf) {
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), getUseText(), 
				new Object[] {player.getDisplayName(), getName() });
	}
	
	/**
	 * Message a player of the results of a Skill invocation and call an event.
	 * @param alf
	 * @param sr
	 */
	private void messageAndEvent(Alf alf, SkillResult sr) {
		Player player = alf.getPlayer();
		if (sr.showMessage) {
			switch (sr.type) {
			case INVALID_TARGET:
				Messaging.send(player, "Invalid Target!", new Object[0]);
				break;
			case LOW_HEALTH:
				Messaging.send(player, "Not enough health!", new Object[0]);
				break;
			case LOW_LEVEL:
				Messaging.send(player, "You must be level $1 to do that.", new Object[] { sr.args[0] });
				break;
			case LOW_MANA:
				Messaging.send(player, "Not enough mana!", new Object[0]);
				break;
			case LOW_STAMINA:
				Messaging.send(player, "You are too fatigued!", new Object[0]);
				break;
			case ON_COOLDOWN:
				Messaging.send(alf.getPlayer(), "Sorry, $1 still has $2 seconds left on cooldown!", new Object[] { sr.args[0], sr.args[1] });
				break;
			case ON_GLOBAL_COOLDOWN:
				Messaging.send(alf.getPlayer(), "Sorry, you must wait $1 seconds longer before using another skill.", new Object[] { sr.args[0] });
				break;
			case MISSING_REAGENT:
				Messaging.send(player, "Sorry, you need to have $1 $2 to use $3!", new Object[] { sr.args[0], sr.args[1], getName() });
				break;
			case NO_COMBAT:
				Messaging.send(player, "You may not use that skill in combat!", new Object[0]);
				break;
			default:
				return;
			}
		}
		SkillCompleteEvent sce = new SkillCompleteEvent(alf, this, sr);
		this.plugin.getServer().getPluginManager().callEvent(sce);
	}
	
	/**
	 * Abstract method for this Skill's usage.
	 * @param alf
	 * @param args
	 * @return
	 */
	public abstract SkillResult use(Alf alf, String[] args);
	
	/**
	 * Executes the skill.
	 * Man, writing this took a while..
	 */
	@SuppressWarnings("deprecation")
	public boolean execute(CommandSender sender, String identifier, String[] args) {
		if (! (sender instanceof Player))
			return false;
		//If player is not an Alf in the system, Skill fails.
		String name = getName();
		Player player = (Player) sender;
		CharacterManager cm = this.plugin.getCharacterManager();
		Alf alf = cm.getAlf(player);
		if (alf == null) {
			Messaging.send(player, "You are not an Alf.", new Object[0], ChatColor.RED);
			return false;
		}
		
		//If player's class types can't use the Skill.
		AlfClass alfClass = alf.getAlfClass();
		AlfClass secondClass = alf.getSecondClass();
		if (! alfClass.hasSkill(name) && (secondClass == null || ! secondClass.hasSkill(name))) {
			Messaging.send(player, "Your classes don't have the skill: $1", 
					new Object[] {name}, ChatColor.RED);
			return true;
		}
		
		//If player has too low of a level to use the Skill, deny the usage.
		int level = SkillConfigManager.getUseSetting(alf, this, Setting.LEVEL, 1, true);
		if (alf.getSkillLevel(this) < level)
			messageAndEvent(alf, new SkillResult(
					SkillResult.ResultType.LOW_LEVEL, true, new Object[] {level}
			));
		
		//If the global value for cooldown has not passed, deny the usage.
		long time = System.currentTimeMillis();
		Long global = alf.getCooldown("global");
		if (global != null && time < global) {
			messageAndEvent(alf, new SkillResult(
					SkillResult.ResultType.ON_GLOBAL_COOLDOWN, true, new Object[] {
							(global - time) / 1000L
					}
			));
			return true;
		}
		
		//Check player specific cooldown.
		int skillLevel = alf.getSkillLevel(this);
		int cooldown = SkillConfigManager.getUseSetting(alf, this, Setting.COOLDOWN, 0, true);
		//Cooldown reduction factor, amplified by Skill level.
		double coolReduce = SkillConfigManager.getUseSetting(
					alf, this, Setting.COOLDOWN_REDUCE, 0.0D, false)*skillLevel;
		cooldown -= (int) coolReduce;
		//If cooldown remains...
		if (cooldown > 0) {
			Long expiry = alf.getCooldown(name);
			if (expiry != null && time < expiry) {
				long remaining = expiry - time;
				messageAndEvent(alf, new SkillResult(
						SkillResult.ResultType.ON_COOLDOWN, true, new Object[] { name, 
								remaining / 1000L
						}));
				return false;
			}
		}
		
		//If the skill can not be used while in combat.
		if (alf.isInCombat() && SkillConfigManager.getUseSetting(alf, this, Setting.NO_COMBAT_USE, false)) {
			messageAndEvent(alf, SkillResult.NO_COMBAT);
			return true;
		}
		
		//Mana cost handling
		int manaCost = SkillConfigManager.getUseSetting(alf, this, Setting.MANA, 0, true);
		double manaReduce = SkillConfigManager.getUseSetting(alf, this, Setting.MANA_REDUCE, 0.0, 
				false)*skillLevel;
		manaCost -= (int) manaReduce;
		
		//Reagent cost handling
		ItemStack itemStack = getReagentCost(alf);
		
		//Health cost handling
		int healthCost = SkillConfigManager.getUseSetting(alf, this, Setting.HEALTH_COST, 0, true);
		double healthReduce = SkillConfigManager.getUseSetting(alf,  this,  Setting.HEALTH_COST_REDUCE, 0.0D, 
				false)*skillLevel;
		healthCost -= (int) healthReduce;
		
		//Stamina cost handling
		int staminaCost = SkillConfigManager.getUseSetting(alf, this, Setting.STAMINA, 0, true);
		double staminaReduce = SkillConfigManager.getUseSetting(alf,  this,  Setting.STAMINA_REDUCE, 0.0D, 
				false)*skillLevel;
		staminaCost -= (int) staminaReduce;
		
		//Call the Skill Event.
		SkillUseEvent skillEvent = new SkillUseEvent(this, player, alf, manaCost, healthCost, staminaCost,
				itemStack, args);
		this.plugin.getServer().getPluginManager().callEvent(skillEvent);
		if (skillEvent.isCancelled()) {
			messageAndEvent(alf, SkillResult.CANCELLED);
			return true;
		}
		
		//Not enough mana
		manaCost = skillEvent.getManaCost();
		if (manaCost > alf.getMana()) {
			messageAndEvent(alf, SkillResult.LOW_MANA);
			return true;
		}
		
		//Not enough health
		healthCost = skillEvent.getHealthCost();
		if (healthCost > 0 && alf.getHealth() <= healthCost) {
			messageAndEvent(alf, SkillResult.LOW_HEALTH);
			return true;
		}
		
		//Not enough stamina
		staminaCost = skillEvent.getStaminaCost();
		if (staminaCost > 0 && alf.getPlayer().getFoodLevel() < staminaCost) {
			messageAndEvent(alf, SkillResult.LOW_STAMINA);
			return true;
		}
		
		//Missing reagents
		itemStack = skillEvent.getReagentCost();
		if (itemStack != null && itemStack.getAmount() != 0 && ! hasReagentCost(player, itemStack)) {
			String reagentName = itemStack.getType().name().toLowerCase().replace("_", " ");
			messageAndEvent(alf, new SkillResult(SkillResult.ResultType.MISSING_REAGENT,
					true, new Object[] { String.valueOf(itemStack.getAmount()), reagentName }));
			return true;
		}
		
		//Slow casting
		int delay = SkillConfigManager.getUseSetting(alf, this, Setting.DELAY, 0, true);
		DelayedSkill dSkill = null;
		if (delay > 0 && !cm.getDelayedSkills().containsKey(alf)) {
			if (addDelayedSkill(alf, delay, identifier, args)) {
				messageAndEvent(alf, SkillResult.START_DELAY);
				if (AlfCore.properties.slowCasting && ! alf.hasEffectType(EffectType.SLOW))
					alf.addEffect(new SlowEffect(this, "Casting", delay, 2, false, "", "", alf));
				return true;
			}
			return true;
		}
		
		//If a delayed skill, handle casting.
		if (cm.getDelayedSkills().containsKey(alf)) {
			dSkill = (DelayedSkill) cm.getDelayedSkills().get(alf);
			if (! dSkill.getSkill().equals(this)) {
				cm.getDelayedSkills().remove(alf);
				alf.setDelayedSkill(null);
				alf.removeEffect(alf.getEffect("Casting"));
				broadcast(player.getLocation(), "$1 has stopped using $2!", new Object[] {
					player.getDisplayName(), dSkill.getSkill().getName()
				});
				
				if (delay > 0) {
					addDelayedSkill(alf, delay, identifier, args);
					if (AlfCore.properties.slowCasting) {
						alf.addEffect(new SlowEffect(this, "Casting", delay, 2, false, "", "", alf));
					}
					messageAndEvent(alf, SkillResult.START_DELAY);
					return true;
				}
				dSkill = null;
			} else {
				if (! dSkill.isReady()) {
					Messaging.send(sender, "You have already begun to use that skill!", new Object[0]);
					return true;
				}
				alf.removeEffect(alf.getEffect("Casting"));
				cm.addCompletedSkill(alf);
			}
		}
		
		SkillResult skillResult;
		
		if (dSkill instanceof DelayedTargetedSkill)
			skillResult = ((TargetedSkill)this).useDelayed(alf, 
					((DelayedTargetedSkill)dSkill).getTarget(), args);
		else
			skillResult = use(alf, args);
		
		if (skillResult.type == SkillResult.ResultType.NORMAL) {
			time = System.currentTimeMillis();
			
			//If cooldown remains...
			if (cooldown > 0) {
				alf.setCooldown(name, time + cooldown);
				AlfCore.debugLog(Level.INFO,
						alf.getName() + " used skill: " + getName() + " cooldown until: " +
						time + cooldown);
			}
			
			//If global cooldown remains....
			if (AlfCore.properties.globalCooldown > 0) {
				alf.setCooldown("global", AlfCore.properties.globalCooldown + time);
			}
			
			//Update exp on cast.
			if (this.awardExpOnCast)
				awardExp(alf);
			
			//Update mana.
			alf.setMana(alf.getMana() - manaCost);
			if (alf.isVerbose() && manaCost > 0) {
				Messaging.send(alf.getPlayer(), ChatColor.BLUE + " MANA " + 
						Messaging.createManaBar(alf.getMana(), alf.getMaxMana()), new Object[0]
				);
			}
			
			//Update health.
			if (healthCost > 0) {
				alf.setHealth(alf.getHealth() - healthCost);
				alf.syncHealth();
			}
			
			//Update stamina.
			if (staminaCost > 0) {
				player.setFoodLevel(player.getFoodLevel() - staminaCost);
			}
			
			//Remove item costs.
			if (itemStack != null && itemStack.getAmount() > 0) {
				player.getInventory().removeItem(new ItemStack[] { itemStack });
				player.updateInventory();
			}
		}
		//Message the result of the skill.
		messageAndEvent(alf, skillResult);
		return true;
	}
}
