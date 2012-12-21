package com.alf.listener;

//import org.bukkit.Bukkit;
//import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
//import org.bukkit.entity.Projectile;
//import org.bukkit.entity.Skeleton;
//import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import com.alf.AlfCore;
//import com.alf.api.event.AlfKillCharacterEvent;
//import com.alf.chararacter.CharacterManager;
//import com.alf.chararacter.CharacterTemplate;

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
//		Player attacker = getAttacker(defender.getLastDamageCause());
//		CharacterManager characterManager = this.plugin.getCharacterManager();
//		CharacterTemplate character = characterManager.getCharacter(defender);
		
		//Suppress dropping of experience as from normal.
		event.setDroppedExp(0);
		
		//Suppress dropping of items, if a player.
		if (defender instanceof Player) {
			//Push death event and inventory into the DeathManager.
			this.plugin.getDeathManager().queuePlayer(event);
			event.getDrops().clear();
		}
		
//		if (attacker != null) {
//			AlfKillCharacterEvent akc = new AlfKillCharacterEvent(character, characterManager.getAlf(attacker));
//			Bukkit.getPluginManager().callEvent(akc);
//		}
//		
//		character.clearEffects();
	}
	
	/**
	 * Get the attacking entity.
	 * @param event
	 * @return
	 */
//	private Player getAttacker(EntityDamageEvent event) {
//		if (event == null)
//			return null;
//		//Whether the damager was an entity.
//		if (event instanceof EntityDamageByEntityEvent) {
//			Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
//			//Return the player damager, if a player.
//			if (damager instanceof Player)
//				return (Player) damager;
//			//If the damaging entity is a Projectile...
//			if (damager instanceof Projectile) {
//				Projectile projectile = (Projectile) damager;
//				//Return the shooting player, if any.
//				if (projectile.getShooter() instanceof Player)
//					return (Player) projectile.getShooter();
//				
//				if (projectile.getShooter() instanceof Skeleton) {
//					CharacterTemplate character = this.plugin.getCharacterManager().getCharacter(projectile.getShooter());
//					if (character.hasEffect("Summon")) {
//						throw new Error("Implement me!");
//					}
//				}
//			} 
//			//If the damaging entity is alive...
//			else if (damager instanceof LivingEntity) {
//				//If the attacker is tamed, return its master.
//				if (damager instanceof Tameable) {
//					Tameable tamed = (Tameable) damager;
//					if (tamed.isTamed() && tamed.getOwner() instanceof Player)
//						return (Player) tamed.getOwner();
//				}
//				//If the damaging entity was summoned, return the summoner.
//				CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity) damager);
//				if (character.hasEffect("Summon")) {
//					throw new Error("Implement me!");
//				}
//			}
//		}
//		//Fire damage
//		else if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK && event.getEntity() instanceof LivingEntity) {
//			CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity)event.getEntity());
//			if (character.hasEffect("Combust")) {
//				throw new Error("Implement me!");
//			}
//		}
//		
//		return null;
//	}
	
	/**
	 * Award experience to the attacker of an entity.
	 * @param attacker
	 * @param defender
	 */
//	private void awardKillExp(Alf attacker, LivingEntity defender) {
//		
//	}
	
}
