package com.alf.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import com.alf.AlfCore;
import com.alf.api.event.AlfEnterCombatEvent;
import com.alf.api.event.AlfKillCharacterEvent;
import com.alf.character.Alf;
import com.alf.character.CharacterManager;
import com.alf.character.CharacterTemplate;
import com.alf.character.Monster;
import com.alf.character.classes.AlfClass;
import com.alf.character.effect.CombatEffect;
import com.alf.character.effect.Effect;
import com.alf.character.effect.common.CombustEffect;
import com.alf.character.effect.common.QuickenEffect;
import com.alf.character.effect.common.SummonEffect;
import com.alf.character.party.AlfParty;
import com.alf.util.Properties;
import com.alf.util.Util;

/**
 * Listens to Entity related events.
 * @author Eteocles
 */
public class AEntityListener implements Listener {
	private final AlfCore plugin;

	public AEntityListener(AlfCore plugin) {
		this.plugin = plugin;
	}

	/**
	 * Handle entity death.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		//
		LivingEntity defender = event.getEntity();
		//
		Player attacker = getAttacker(defender.getLastDamageCause());
		CharacterManager characterManager = this.plugin.getCharacterManager();
		CharacterTemplate character = characterManager.getCharacter(defender);

		//Suppress dropping of experience as from normal.
		event.setDroppedExp(0);

		//Suppress dropping of items, if a player.
		if (defender instanceof Player) {
			//Push death event and inventory into the DeathManager.
			this.plugin.getDeathManager().queuePlayer(event);
			event.getDrops().clear();
		}

		if (attacker != null) {
			AlfKillCharacterEvent akc = new AlfKillCharacterEvent(character, characterManager.getAlf(attacker));
			Bukkit.getPluginManager().callEvent(akc);
		}

		Alf alfDefender;
		if (defender instanceof Player) {
			Player player = (Player) defender;
			alfDefender = (Alf) character;
			//Add to list of deaths.
			Util.deaths.put(player.getName(), event.getEntity().getLocation());
			alfDefender.cancelDelayedSkill();

			double multiplier = AlfCore.properties.expLoss;
			if (attacker != null)
				multiplier = AlfCore.properties.pvpExpLossMultiplier;

			//Cause defender to leave combat.
			if (alfDefender.isInCombat() && defender.getLastDamageCause() != null) {
				if (defender.getLastDamageCause().getCause() != EntityDamageEvent.DamageCause.SUICIDE)
					alfDefender.leaveCombat(CombatEffect.LeaveCombatReason.DEATH);
				else
					alfDefender.leaveCombat(CombatEffect.LeaveCombatReason.SUICIDE);
			}

			//Cause defender to lose exp from death.
			alfDefender.loseExpFromDeath(multiplier, attacker != null);

			//Remove all non-persistent effects.
			for (Effect effect : alfDefender.getEffects())
				if (! effect.isPersistent())
					alfDefender.removeEffect(effect);
		}
		//Award experience to the killing player, if it exists.
		if (attacker != null && ! attacker.equals(defender) && defender instanceof LivingEntity) {
			Alf alf = characterManager.getAlf(attacker);
			awardKillExp(alf, defender);
		}
		character.clearEffects();
	}

	/**
	 * Get the attacking entity.
	 * @param event
	 * @return
	 */
	private Player getAttacker(EntityDamageEvent event) {
		if (event == null)
			return null;
		//Whether the damager was an entity.
		if (event instanceof EntityDamageByEntityEvent) {
			Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
			//Return the player damager, if a player.
			if (damager instanceof Player)
				return (Player) damager;
			//If the damaging entity is a Projectile...
			if (damager instanceof Projectile) {
				Projectile projectile = (Projectile) damager;
				//Return the shooting player, if any.
				if (projectile.getShooter() instanceof Player)
					return (Player) projectile.getShooter();

				if (projectile.getShooter() instanceof Skeleton) {
					CharacterTemplate character = this.plugin.getCharacterManager().getCharacter(projectile.getShooter());
					if (character.hasEffect("Summon")) {
						SummonEffect sEffect = (SummonEffect)character.getEffect("Summon");
						return sEffect.getSummoner().getPlayer();
					}
				}
			} 
			//If the damaging entity is alive...
			else if (damager instanceof LivingEntity) {
				//If the attacker is tamed, return its master.
				if (damager instanceof Tameable) {
					Tameable tamed = (Tameable) damager;
					if (tamed.isTamed() && tamed.getOwner() instanceof Player)
						return (Player) tamed.getOwner();
				}
				//If the damaging entity was summoned, return the summoner.
				CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity) damager);
				if (character.hasEffect("Summon")) {
					SummonEffect sEffect = (SummonEffect)character.getEffect("Summon");
					return sEffect.getSummoner().getPlayer();
				}
			}
		}
		//Fire damage
		else if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK && event.getEntity() instanceof LivingEntity) {
			CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity)event.getEntity());
			if (character.hasEffect("Combust")) {
				return ((CombustEffect)character.getEffect("Combust")).getApplier();
			}
		}

		return null;
	}

	/**
	 * Award experience to the attacker of an entity.
	 * @param attacker
	 * @param defender
	 */
	private void awardKillExp(Alf attacker, LivingEntity defender) {
		Properties prop = AlfCore.properties;

		double addedExp = 0.0D;
		AlfClass.ExperienceType experienceType = null;

		//Can't get exp for killing your summons.
		if (attacker.getSummons().contains(defender))
			return;

		if (defender instanceof Player) {
			//Is this redundant...
			//			Util.deaths.put(((Player)defender).getName(), defender.getLocation());
			addedExp = prop.playerKillingExp;
			int aLevel = attacker.getTieredLevel(false);
			int dLevel = this.plugin.getCharacterManager().getAlf((Player) defender).getTieredLevel(false);
			addedExp *= findExpAdjustment(aLevel, dLevel);
			experienceType = AlfClass.ExperienceType.PVP;
		} else if (defender instanceof LivingEntity && ! (defender instanceof Player)) {
			Monster monster = this.plugin.getCharacterManager().getMonster(defender);
			addedExp = monster.getExperience();

			//If invalid exp return value and doesn't exist in properties, quit.
			if (addedExp == -1.0D && ! prop.creatureKillingExp.containsKey(defender.getType()))
				return;
			if (addedExp == -1.0D)
				addedExp = prop.creatureKillingExp.get(defender.getType());
			experienceType = AlfClass.ExperienceType.KILLING;

			//Reduce exp amount if monster spawned from spawner.
			if (prop.noSpawnCamp && monster.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
				addedExp *= prop.spawnCampExpMult;
		}

		if (experienceType != null && addedExp > 0.0D)
			if (attacker.hasParty()) {
				attacker.getParty().gainExp(addedExp, experienceType, defender.getLocation());
			} else if (attacker.canGain(experienceType))
				attacker.gainExp(addedExp, experienceType, defender.getLocation());
	}
	
	/**
	 * Handle entity target.
	 * @param event
	 */
	@EventHandler(ignoreCancelled=true)
	public void onEntityTarget(EntityTargetEvent event) {
		if (!(event.getTarget() instanceof Player)) {
			return;
		}

		Alf alf = this.plugin.getCharacterManager().getAlf((Player)event.getTarget());
		if ((alf.hasEffect("Invisible")) || (alf.hasEffect("Invuln")))
			event.setCancelled(true);
	}
	
	/**
	 * Handle entity regaining health.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if (! (event.getEntity() instanceof Player))
			return;
		
		Player player = (Player) event.getEntity();
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		
		if (alf.hasParty()) {
			AlfParty party = alf.getParty();
			if (event.getAmount() > 0)
				party.update();
		}
	}
	
	/**
	 * Handle entity damaging.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getDamage() == 0 || event.getEntity().isDead() || 
				! (event.getEntity() instanceof LivingEntity)) 
			return;
		
		LivingEntity defender = (LivingEntity) event.getEntity();
		LivingEntity attacker = getAttacker(event);
		
		if (attacker == null)
			return;
		
		//Enter combat.
		if (attacker instanceof Player) {
			Alf alf = this.plugin.getCharacterManager().getAlf((Player) attacker);
			if (alf.isInCombatWith(defender))
				alf.refreshCombat();
			else {
				CombatEffect.CombatReason reason = (defender instanceof Player) ?
						CombatEffect.CombatReason.ATTACKED_PLAYER : CombatEffect.CombatReason.ATTACKED_MOB;
				this.plugin.getServer().getPluginManager().callEvent(
						new AlfEnterCombatEvent(alf, defender, reason));
				alf.enterCombatWith(defender, reason);
			}
		}
		
		if (defender instanceof Player) {
			Alf alf = this.plugin.getCharacterManager().getAlf((Player) defender);
			if (alf.isInCombatWith(attacker))
				alf.refreshCombat();
			else {
				CombatEffect.CombatReason reason = (defender instanceof Player) ?
						CombatEffect.CombatReason.DAMAGED_BY_PLAYER : CombatEffect.CombatReason.DAMAGED_BY_MOB;
				this.plugin.getServer().getPluginManager().callEvent(
						new AlfEnterCombatEvent(alf, attacker, reason));
				alf.enterCombatWith(attacker, reason);
			}
			for (Effect e : alf.getEffects())
				if (e instanceof QuickenEffect)
					e.reapplyToAlf(alf);
		}
	}

	/**
	 * Handle creature spawning.
	 * @param event
	 */
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			Monster monster = new Monster(this.plugin, event.getEntity());
			monster.setSpawnReason(event.getSpawnReason());
			this.plugin.getCharacterManager().addMonster(monster);
		}
	}

	/**
	 * Find the scaling factor for an attacker of a certain level and a defender of a certain level.
	 * @param aLevel
	 * @param dLevel
	 * @return
	 */
	private double findExpAdjustment(int aLevel, int dLevel) {
		int diff = aLevel - dLevel;
		if (Math.abs(diff) <= AlfCore.properties.pvpExpRange)
			return 1.0D;
		if (diff >= AlfCore.properties.pvpMaxExpRange)
			return 0.0D;
		if (diff <= -AlfCore.properties.pvpMaxExpRange)
			return 2.0D;
		//Attacking level is higher than defending level.
		if (diff > 0)
			return 1.0D - (diff - AlfCore.properties.pvpExpRange) / AlfCore.properties.pvpMaxExpRange;
		//Defending level is higher than attacking level.
		if (diff < 0)
			return 1.0D + (Math.abs(diff) - AlfCore.properties.pvpExpRange) / AlfCore.properties.pvpMaxExpRange;
		return 1.0D;
	}

}
