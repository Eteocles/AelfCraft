package com.alf.listener;

import net.minecraft.server.v1_4_5.EntityLiving;
import net.minecraft.server.v1_4_5.MobEffectList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import com.alf.AlfCore;
import com.alf.api.AlfAttackDamageCause;
import com.alf.api.AlfDamageCause;
import com.alf.api.AlfSkillDamageCause;
import com.alf.api.SkillUseInfo;
import com.alf.api.event.CharacterDamageEvent;
import com.alf.api.event.SkillDamageEvent;
import com.alf.api.event.WeaponDamageEvent;
import com.alf.character.Alf;
import com.alf.character.CharacterDamageManager;
import com.alf.character.CharacterTemplate;
import com.alf.character.Monster;
import com.alf.character.effect.Effect;
import com.alf.character.effect.EffectType;
import com.alf.character.party.AlfParty;
import com.alf.skill.Skill;
import com.alf.skill.SkillType;
import com.alf.util.Messaging;
import com.alf.util.Util;

/**
 * Handles damage events.
 * @author Eteocles
 */
public class ADamageListener implements Listener {
	
	private AlfCore plugin;
	private CharacterDamageManager dm;
	
	/**
	 * Construct the listener.
	 * @param plugin
	 * @param damageManager
	 */
	public ADamageListener(AlfCore plugin, CharacterDamageManager damageManager) {
		this.plugin = plugin;
		this.dm = damageManager;
	}
	
	/**
	 * Handle potion damage.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		if (event.getAffectedEntities().isEmpty() ||
				! (event.getPotion().getShooter() instanceof Player))
			return;
		
		boolean remove = false;
		
		for (PotionEffect effect : event.getPotion().getEffects()) {
			switch (effect.getType().getId()) {
			case 2:
				//SLOW
			case 4:
				//SLOW_DIGGING
			case 7:
				//HARM
			case 9:
				//CONFUSION
			case 15:
				//BLINDNESS
			case 17:
				//HUNGER
			case 18:
				//WEAKNESS
			case 19:
				//POISON
			case 20:
				//WITHER
				remove = true;
				break;
			case 3:
				//FAST_DIGGING
			case 5:
				//INCREASE_DAMAGE
			case 6:
				//HEAL
			case 8:
				//JUMP
			case 10:
				//REGENERATION
			case 11:
				//DAMAGE_RESISTANCE
			case 12:
				//FIRE_RESISTANCE
			case 13:
				//WATER_BREATHING
			case 14:
				//INVISIBILITY
			case 16:
				//NIGHT_VISION
			}
		}
		Alf alf;
		if (remove) {
			alf = this.plugin.getCharacterManager().getAlf((Player)event.getPotion().getShooter());
			for (LivingEntity le : event.getAffectedEntities())
				if (! Skill.damageCheck(alf.getPlayer(), le))
					//If no damage is dealt, then set the impact of this event to 0.
					event.setIntensity(le, 0.0D);
		}
	}
	
	/**
	 * Handle entity health regeneration.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if (! (event.getEntity() instanceof Player))
			return;
		
		int amount = event.getAmount();
		Player player = (Player) event.getEntity();
		final Alf alf = this.plugin.getCharacterManager().getAlf(player);
		int maxHealth = alf.getMaxHealth();
		double healPercent;
		
		switch (event.getRegainReason()) {
		case CUSTOM:
			break;
		case EATING:
			healPercent = AlfCore.properties.foodHealPercent;
			amount = (int) Math.ceil(maxHealth * healPercent);
			break;
		case ENDER_CRYSTAL:
			break;
		case MAGIC:
			healPercent = amount / 6.0D;
			amount = (int) Math.ceil(healPercent * AlfCore.properties.potHealthPerTier * alf.getMaxHealth());
			break;
		case MAGIC_REGEN:
			break;
		case REGEN:
			healPercent = amount / 20.0D;
			amount = (int) Math.ceil(alf.getMaxHealth() * healPercent);
			break;
		case SATIATED:
			break;
		case WITHER:
			break;
		case WITHER_SPAWN:
			break;
		default:
			break;
		}
		
		//Alf health calculation.
		int newAlfHealth = alf.getHealth() + amount;
		if (newAlfHealth > maxHealth)
			newAlfHealth = maxHealth;
		
		//Scale alf health to match in-game health.
		int newPlayerHealth = newAlfHealth / maxHealth * 20;
		alf.setHealth(newAlfHealth);
		
		//Can't get negative health regain.
		int newAmount = newPlayerHealth - player.getHealth();
		if (newAmount < 0)
			newAmount = 0;
		
		//Sync and update health.
		event.setAmount(newAmount);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				alf.syncHealth();
			}
		});
	}
	
	/**
	 * Handle entity damage events.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		Entity defender = event.getEntity();
		Entity attacker = null;
		AlfDamageCause lastDamage = null;
		int damage = event.getDamage();
		boolean skipEvent = false;
		
		//If the atttacker was an entity, get the attacking entity.
		if (event instanceof EntityDamageByEntityEvent) {
			attacker = ((EntityDamageByEntityEvent)event).getDamager();
		}
		
		//Cancel handling if defender is dead or in creative mode.
		if (defender instanceof LivingEntity) {
			if (defender.isDead() || ((LivingEntity)defender).getHealth() <= 0)
				return;
			if (defender instanceof Player) {
				Player player = (Player) defender;
				if (player.getGameMode() == GameMode.CREATIVE)
					return;
				lastDamage = this.plugin.getCharacterManager().getAlf((Player) defender).getLastDamageCause();
			}
		}
		
		//If defender is a spell target.
		if (this.dm.isSpellTarget(defender)) {
			skipEvent = true;
			//Outsource to spell damage handling.
			damage = onSpellDamage(event, damage, defender);
		} else {
			EntityDamageEvent.DamageCause cause = event.getCause();
			
			switch (cause) {
			case SUICIDE:
				if (defender instanceof Player) {
					skipEvent = true;
					Player player = (Player) event.getEntity();
					this.plugin.getCharacterManager().getAlf(player).setHealth(0);
					//Find out who last damaged.
					if (player.getLastDamageCause() != null && 
							player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
						Entity tempDamager = ((EntityDamageByEntityEvent)player.getLastDamageCause()).getDamager();
						player.setLastDamageCause(new EntityDamageByEntityEvent(tempDamager, player, 
								EntityDamageEvent.DamageCause.ENTITY_ATTACK, 1000));
						player.damage(1000, tempDamager);
					} else {
						event.setDamage(1000);
						return;
					}
				}
			case DROWNING:
				damage = onEntityDrown(event.getDamage(), defender, event);
				break;
			case ENTITY_ATTACK:
			case ENTITY_EXPLOSION:
			case PROJECTILE:
				skipEvent = true;
				damage = onEntityDamageCore(event, attacker, damage);
				break;
			case FALL:
				damage = onEntityFall(event.getDamage(), defender, event);
				break;
			case FALLING_BLOCK:
				break;
			case FIRE:
			case FIRE_TICK:
			case LAVA:
				damage = onEntityFlame(event.getDamage(), cause, defender, event);
				break;
			case LIGHTNING:
				break;
			case MAGIC:
				break;
			case MELTING:
				break;
			case POISON:
				damage = onEntityPoison(event.getDamage(), defender, event);
				break;
			case STARVATION:
				damage = onEntityStarve(event.getDamage(), defender);
				break;
			case SUFFOCATION:
				damage = onEntitySuffocate(event.getDamage(), defender);
				break;
			case VOID:
				break;
			case WITHER:
				break;
			default:
				break;
			}
			
			if (event.isCancelled()) {
				if (defender instanceof Player)
					this.plugin.getCharacterManager().getAlf((Player) defender).setLastDamageCause(lastDamage);
				return;
			}
		}
		
		//If the defender is a player...
		if (defender instanceof Player) {
			Player player = (Player) defender;
			if (player.getNoDamageTicks() > 10 && damage > 0 || player.isDead() || player.getHealth() <= 0) {
				event.setCancelled(true);
				return;
			}
			
			//Get the Alf rep.
			final Alf alf = this.plugin.getCharacterManager().getAlf(player);
			//Check inventory.
			alf.checkInventory();
			//If player is invulnerable, cancel.
			if (alf.hasEffectType(EffectType.INVULNERABILITY)) {
				event.setCancelled(true);
				return;
			}
			
			//Remove Invisibility effect on damage.
			for (Effect effect : alf.getEffects()) {
				if (effect.isType(EffectType.INVIS))
					alf.removeEffect(effect);
			}
			
			if (attacker instanceof Projectile)
				attacker = ((Projectile) attacker).getShooter();
			
			if (attacker instanceof Player) {
				//Attacking level.
				int aLevel = this.plugin.getCharacterManager().getAlf((Player) attacker).getTieredLevel(false);
				//Defender level.
				int alfLevel = alf.getTieredLevel(false);
				//Outside pvp level range.
				if (Math.abs(aLevel - alfLevel) > AlfCore.properties.pvpLevelRange) {
					Messaging.send((Player) attacker, "That player is outside of your level range!", new Object[0], 
							ChatColor.RED);
					event.setCancelled(true);
					return;
				}
				//Not high enough level to pvp.
				if (alfLevel < AlfCore.properties.minPvpLevel || aLevel < AlfCore.properties.minPvpLevel) {
					event.setCancelled(true);
					Messaging.send((Player) attacker, "You or your target is not high enough level to PvP!",
							new Object[0], ChatColor.RED);
					return;
				}
				//Same party member.
				AlfParty party = alf.getParty();
				if (party != null && party.isNoPvp() && 
						party.isPartyMember((Player) attacker)) {
					event.setCancelled(true);
					return;
				}
			}
			
			//If event wasn't handled through other methods...
			if (! skipEvent) {
				CharacterDamageEvent cde = new CharacterDamageEvent(defender, 
						event.getCause(), damage);
				Bukkit.getPluginManager().callEvent(cde);
				if (cde.isCancelled()) {
					event.setCancelled(true);
					alf.setLastDamageCause(lastDamage);
					return;
				}
				damage = cde.getDamage();
			}
			
			if (damage == 0) {
				event.setDamage(0);
				return;
			}
			
			switch (event.getCause()) {
			case BLOCK_EXPLOSION:
			case CONTACT:
			case ENTITY_ATTACK:
			case ENTITY_EXPLOSION:
			case LAVA:
			case FIRE:
			case PROJECTILE:
			case FALLING_BLOCK:
				alf.setHealth(alf.getHealth() - (int) Math.ceil(damage * calculateArmorReduction(player.getInventory())));
				break;
			case DROWNING:
			case FIRE_TICK:
			case FALL:
			case LIGHTNING:
			case POISON:
			case SUFFOCATION:
			case MAGIC: //Put this in armor reduction?
			case STARVATION:
			case VOID:
			case WITHER:
			default:
				alf.setHealth(alf.getHealth() - damage);
			}
			
			//Set the damage for the event.
			event.setDamage(convertAlfDamage(damage, alf));
			
			if (alf.getHealth() != 0 && player.getHealth() == 1 && event.getDamage() == 1)
				player.setHealth(2);
			
			if (alf.getHealth() == 0)
				event.setDamage(200);
			else {
				//Synchronize health with the plugin.
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
					public void run() {
						alf.syncHealth();
					}
				}, 1L);
			}
			
			AlfParty party = alf.getParty();
			if (party != null && damage > 0)
				party.update();
		}
		//If the defender is a LivingEntity (and not a Player)
		else if (defender instanceof LivingEntity) {
			if ( ( ((CraftLivingEntity) defender).getNoDamageTicks() > 10 && damage > 0) || defender.isDead() ||
					((LivingEntity)defender).getHealth() <= 0) {
				event.setCancelled(true);
				return;
			}
			
			LivingEntity lEntity = (LivingEntity) defender;
			Monster monster = this.plugin.getCharacterManager().getMonster(lEntity);
			int currentHealth = monster.getHealth();
			
			switch (event.getCause()) {
			case BLOCK_EXPLOSION:
			case CONTACT:
			case ENTITY_ATTACK:
			case ENTITY_EXPLOSION:
			case LAVA:
			case FIRE:
			case PROJECTILE:
			case FALL:
			case FALLING_BLOCK:
				damage = (int)(damage * calculateArmorReduction(lEntity));
			case DROWNING:
			case FIRE_TICK:
			case STARVATION:
			case LIGHTNING:
			case POISON:
			case SUFFOCATION:
			case MAGIC:
			case MELTING:
			case WITHER:
			default:
			}
			
			currentHealth -= damage;
			
			if (currentHealth <= 0) {
				monster.setHealth(0);
				damage = 10000;
			} else {
				monster.setHealth(currentHealth);
				damage = convertAlfDamage(damage, monster);
				int newHealth = lEntity.getHealth() - damage;
				
				//
				if (newHealth <= 0 && lEntity.getHealth() + 1 - newHealth > lEntity.getMaxHealth()) {
					damage = damage + newHealth - 1;
					if (damage < 1)
						if (lEntity.getHealth() + 1 <= lEntity.getMaxHealth()) {
							lEntity.setHealth(lEntity.getHealth() + 1);
							damage = 1;
						} else
							damage = 0;
				} else if (newHealth <= 0)
					lEntity.setHealth(lEntity.getHealth() + 1 - newHealth);
				
			}
			event.setDamage(damage);
		}
	}
	
	/**
	 * Core method for handling damage events.
	 * @param event
	 * @param attacker
	 * @param damage
	 * @return
	 */
	private int onEntityDamageCore(EntityDamageEvent event, Entity attacker, int damage) {
		if (event.getDamage() == 0)
			return 0;
		
		CharacterTemplate character = null;
		
		//Attacker is a player.
		if (attacker instanceof Player) {
			Player attackingPlayer = (Player) attacker;
			Alf alf = this.plugin.getCharacterManager().getAlf(attackingPlayer);
			character = alf;
			//If the attacker can't equip the item it's attacking with, or if it's stunned, cancel.
			if (! alf.canEquipItem(attackingPlayer.getInventory().getHeldItemSlot()) ||
					alf.hasEffectType(EffectType.STUN)) {
				event.setCancelled(true);
				return 0;
			}
			//
			damage = getPlayerDamage(attackingPlayer, damage);
		}
		//Attacker is a Living Entity that's not a player.
		else if (attacker instanceof LivingEntity) {
			Monster monster = this.plugin.getCharacterManager().getMonster((LivingEntity) attacker);
			character = monster;
			damage = monster.getDamage();
		}
		//Attacker is a Projectile
		else if (attacker instanceof Projectile) {
			Projectile projectile = (Projectile) attacker;
			if (projectile.getShooter() instanceof Player) {
				attacker = projectile.getShooter();
				character = this.plugin.getCharacterManager().getAlf((Player) attacker);
				damage = getPlayerProjectileDamage((Player) projectile.getShooter(), projectile, damage);
				//Amplify damage by velocity.
				damage = (int) Math.ceil(damage / 3.0D * projectile.getVelocity().length());
			} else if (projectile.getShooter() instanceof LivingEntity) {
				attacker = projectile.getShooter();
				Monster monster = this.plugin.getCharacterManager().getMonster(projectile.getShooter());
				character = monster;
				damage = monster.getDamage();
			}
		}
		
		
		//Attacker is a character (Alf or Monster from previous section).
		if (character != null) {
			WeaponDamageEvent wde = new WeaponDamageEvent(damage, (EntityDamageByEntityEvent) event, character);
			this.plugin.getServer().getPluginManager().callEvent(wde);
			if (wde.isCancelled()) {
				event.setCancelled(true);
				return 0;
			}
			damage = wde.getDamage();
		}
		
		//Person being attacked is a player.
		if (event.getEntity() instanceof Player) {
			Alf alf = this.plugin.getCharacterManager().getAlf((Player) event.getEntity());
			alf.setLastDamageCause(new AlfAttackDamageCause(damage, event.getCause(), attacker));
		}
		
		return damage;
	}
	
	/**
	 * Handle spell damage.
	 * @param event
	 * @param damage
	 * @param defender
	 * @return
	 */
	private int onSpellDamage(EntityDamageEvent event, int damage, Entity defender) {
		SkillUseInfo skillInfo = this.dm.removeSpellTarget(defender);
		
		if (event instanceof EntityDamageByEntityEvent) {
			if (resistanceCheck(defender, skillInfo.getSkill())) {
				skillInfo.getSkill().broadcast(defender.getLocation(),
						"$1 has resisted $2", new Object[] {
					Messaging.getLivingEntityName((LivingEntity) defender), skillInfo.getSkill().getName()
				});
				event.setCancelled(true);
				return 0;
			}
			
			SkillDamageEvent spellDamageEvent = new SkillDamageEvent(damage, defender, skillInfo);
			this.plugin.getServer().getPluginManager().callEvent(spellDamageEvent);
			
			//If the event is cancelled, no damaged is dealt.
			if (spellDamageEvent.isCancelled()) {
				event.setCancelled(true);
				return 0;
			}
			
			damage = spellDamageEvent.getDamage();
			if (defender instanceof Player) {
				this.plugin.getCharacterManager().getAlf((Player) defender).setLastDamageCause(
						new AlfSkillDamageCause(damage, event.getCause(), skillInfo.getCharacter().getEntity(),
								skillInfo.getSkill()));
			}
		}
		
		return damage;
	}

	/**
	 * Handle entity starvation.
	 * @param defaultDamage
	 * @param entity
	 * @return
	 */
	private int onEntityStarve(double defaultDamage, Entity entity) {
		if (! (entity instanceof LivingEntity))
			return 0;
		
		Double percent = this.dm.getEnvironmentalDamage(EntityDamageEvent.DamageCause.STARVATION);
		if (percent == null) {
			if (entity instanceof Player) {
				Alf alf = this.plugin.getCharacterManager().getAlf((Player) entity);
				alf.setLastDamageCause(new AlfDamageCause((int) defaultDamage, EntityDamageEvent.DamageCause.STARVATION));
			}
			return (int) defaultDamage;
		}
		
		CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity) entity);
		percent = percent * character.getMaxHealth();
		
		if (character instanceof Alf)
			((Alf)character).setLastDamageCause(new AlfDamageCause(percent.intValue(), EntityDamageEvent.DamageCause.STARVATION));
		
		
		return percent < 1.0D ? 1 : percent.intValue();
	}
	
	/**
	 * Handle entity suffocation.
	 * @param defaultDamage
	 * @param entity
	 * @return - damage dealt
	 */
	private int onEntitySuffocate(double defaultDamage, Entity entity) {
		if (! (entity instanceof LivingEntity))
			return 0;
		Double percent = this.dm.getEnvironmentalDamage(EntityDamageEvent.DamageCause.SUFFOCATION);
		if (percent == null) {
			if (entity instanceof Player) {
				Alf alf = this.plugin.getCharacterManager().getAlf((Player) entity);
				alf.setLastDamageCause(new AlfDamageCause((int) defaultDamage, EntityDamageEvent.DamageCause.SUFFOCATION));
			}
			return (int) defaultDamage;
		}
		
		CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity) entity);
		percent = percent * character.getMaxHealth();
		
		if (character instanceof Alf)
			((Alf)character).setLastDamageCause(new AlfDamageCause(percent.intValue(), EntityDamageEvent.DamageCause.SUFFOCATION));
		
		return percent < 1.0D ? 1 : percent.intValue();
	}
	
	/**
	 * Handle entity drowning.
	 * @param defaultDamage
	 * @param entity
	 * @param event
	 * @return
	 */
	private int onEntityDrown(double defaultDamage, Entity entity, EntityDamageEvent event) {
		if (! (entity instanceof LivingEntity))
			return 0;
		
		Double percent = this.dm.getEnvironmentalDamage(EntityDamageEvent.DamageCause.DROWNING);
		if (percent == null) {
			if (entity instanceof Player) {
				Alf alf = this.plugin.getCharacterManager().getAlf((Player) entity);
				alf.setLastDamageCause(new AlfDamageCause((int) defaultDamage, EntityDamageEvent.DamageCause.DROWNING));
			}
			return (int) defaultDamage;
		}
		
		CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity) entity);
		if (character.hasEffectType(EffectType.WATER_BREATHING)) {
			event.setCancelled(true);
			return 0;
		}
		
		percent = percent * character.getMaxHealth();
		if (character instanceof Alf)
			((Alf) character).setLastDamageCause(new AlfDamageCause(percent.intValue(), EntityDamageEvent.DamageCause.DROWNING));
		
		return percent < 1.0D ? 1 : percent.intValue();
	}
	
	/**
	 * Handle entity poisoning.
	 * @param defaultDamage
	 * @param entity
	 * @param event
	 * @return
	 */
	private int onEntityPoison(double defaultDamage, Entity entity, EntityDamageEvent event) {
		if (! (entity instanceof LivingEntity))
			return 0;
		
		Double damage = this.dm.getEnvironmentalDamage(EntityDamageEvent.DamageCause.POISON);
		
		if (damage == null) {
			if (entity instanceof Player) {
				Alf alf = this.plugin.getCharacterManager().getAlf((Player) entity);
				alf.setLastDamageCause(new AlfDamageCause((int) defaultDamage, EntityDamageEvent.DamageCause.POISON));
			}
			return (int) defaultDamage;
		}
		
		//Cancel if resist poison.
		CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity) entity);
		if (character.hasEffectType(EffectType.RESIST_POISON)) {
			event.setCancelled(true);
			return 0;
		}
		
		//Set the last damage cause for the Alf.
		if (character instanceof Alf)
			((Alf)character).setLastDamageCause(new AlfDamageCause(damage.intValue(), 
					EntityDamageEvent.DamageCause.POISON));
		
		return damage.intValue();
	}
	
	/**
	 * Handle entity flame damage.
	 * @param defaultDamage
	 * @param cause
	 * @param entity
	 * @param event
	 * @return
	 */
	private int onEntityFlame(double defaultDamage, EntityDamageEvent.DamageCause cause,
			Entity entity, EntityDamageEvent event) {
		Double damage = this.dm.getEnvironmentalDamage(cause);
		
		//If the damage cause wasn't registered...
		if (damage == null) {
			if (entity instanceof Player) {
				Alf alf = this.plugin.getCharacterManager().getAlf((Player) entity);
				alf.setLastDamageCause(new AlfDamageCause((int) defaultDamage, cause));
			}
			return (int) defaultDamage;
		}
		
		CharacterTemplate character = null;
		
		//If entity has fire resistance, deal no damage.
		if (entity instanceof LivingEntity) {
			EntityLiving el = ((CraftLivingEntity)entity).getHandle();
			if (el.hasEffect(MobEffectList.FIRE_RESISTANCE)) {
				event.setCancelled(true);
				return 0;
			}
			character = this.plugin.getCharacterManager().getCharacter((LivingEntity) entity);
		}
		
		if (damage == 0.0D)
			return 0;
		
		if (character != null) {
			//If the character has fire resistance effect type.
			if (character.hasEffectType(EffectType.RESIST_FIRE)) {
				event.setCancelled(true);
				return 0;
			}
			
			if (cause != EntityDamageEvent.DamageCause.FIRE_TICK)
				damage = damage * character.getMaxHealth();
			if (character instanceof Alf)
				((Alf) character).setLastDamageCause(new AlfDamageCause(damage.intValue(), cause));
		}
		
		return damage < 1.0D ? 1 : damage.intValue();
	}
	
	/**
	 * Handle entity fall.
	 * @param damage
	 * @param entity
	 * @param event
	 * @return
	 */
	private int onEntityFall(int damage, Entity entity, EntityDamageEvent event) {
		if (! (entity instanceof LivingEntity)) 
			return 0;
		
		Double damagePercent = this.dm.getEnvironmentalDamage(EntityDamageEvent.DamageCause.FALL);
		
		if (damagePercent == null)
			return damage;
		
		//Reduce damage by feather fall calculation.
		if (entity instanceof HumanEntity)
			damage -= Util.getFeatherFallLevel(((HumanEntity)entity).getInventory());
		
		//Cancel the event if no damage was dealt.
		if (damage <= 0) {
			event.setCancelled(true);
			return 0;
		}
		
		CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity) entity);
		if (character.hasEffectType(EffectType.SAFEFALL)) {
			event.setCancelled(true);
			return 0;
		}
		
		damage = (int) (damage * damagePercent * character.getMaxHealth());
		return (damage < 1) ? 1 : damage;
	}
	
	/**
	 * Calculate armor reduction for a living entity.
	 * @param lEntity
	 * @return
	 */
	private double calculateArmorReduction(LivingEntity lEntity) {
		if (lEntity instanceof Player)
			return calculateArmorReduction( ((Player)lEntity).getInventory() );
		int armor = 0;
		
		switch (lEntity.getType()) {
		case SKELETON:
		case ZOMBIE:
			armor = 2;
			break;
		case MAGMA_CUBE:
			MagmaCube mc = (MagmaCube) lEntity;
			switch (mc.getSize()) {
			case 1:
				armor = 3;
				break;
			case 2:
				armor = 6;
				break;
			case 3:
				armor = 12;
			}
			break;
		default:
			break;
		}
		
		armor += calculateArmorReduction(lEntity.getEquipment());
		
		if (armor >= 25)
			armor = 24;
		
		return (25.0D - armor) / 25.0D;
	}
	
	/**
	 * Get the armor reduction for an entity armor equip.
	 * @param inventory
	 * @return
	 */
	private double calculateArmorReduction(EntityEquipment inventory) {
		int armorPoints = 0;
		
		for (ItemStack armor : inventory.getArmorContents()) {
			if (armor != null) {
				switch (armor.getType()) {
				case LEATHER_HELMET:
				case LEATHER_BOOTS:
				case GOLD_BOOTS:
					armorPoints++;
					break;
				case LEATHER_LEGGINGS:
				case GOLD_HELMET:
				case CHAINMAIL_HELMET:
				case IRON_HELMET:
				case IRON_BOOTS:
					armorPoints += 2;
					break;
				case LEATHER_CHESTPLATE:
				case GOLD_LEGGINGS:
				case DIAMOND_HELMET:
				case DIAMOND_BOOTS:
					armorPoints += 3;
					break;
				case CHAINMAIL_LEGGINGS:
					armorPoints += 4;
					break;
				case GOLD_CHESTPLATE:
				case CHAINMAIL_CHESTPLATE:
				case IRON_LEGGINGS:
					armorPoints += 5;
					break;
				case IRON_CHESTPLATE:
				case DIAMOND_LEGGINGS:
					armorPoints += 6;
					break;
				case DIAMOND_CHESTPLATE:
					armorPoints += 8;
				default:
					break;
				}
			}
		}
		
		return armorPoints;
	}
	
	/**
	 * Calculate damage reduction by armor.
	 * @param inventory
	 * @return
	 */
	private double calculateArmorReduction(PlayerInventory inventory) {
		int armorPoints = 0;
		
		for (ItemStack armor : inventory.getArmorContents()) {
			if (armor != null) {
				switch (armor.getType()) {
				case LEATHER_HELMET:
				case LEATHER_BOOTS:
				case GOLD_BOOTS:
					armorPoints++;
					break;
				case LEATHER_LEGGINGS:
				case GOLD_HELMET:
				case CHAINMAIL_HELMET:
				case IRON_HELMET:
				case IRON_BOOTS:
					armorPoints += 2;
					break;
				case LEATHER_CHESTPLATE:
				case GOLD_LEGGINGS:
				case DIAMOND_HELMET:
				case DIAMOND_BOOTS:
					armorPoints += 3;
					break;
				case CHAINMAIL_LEGGINGS:
					armorPoints += 4;
					break;
				case GOLD_CHESTPLATE:
				case CHAINMAIL_CHESTPLATE:
				case IRON_LEGGINGS:
					armorPoints += 5;
					break;
				case IRON_CHESTPLATE:
				case DIAMOND_LEGGINGS:
					armorPoints += 6;
					break;
				case DIAMOND_CHESTPLATE:
					armorPoints += 8;
				default:
					break;
				}
			}
		}
		
		return (25 - armorPoints) / 25.0D;
	}
	
	/**
	 * Get the player damage dealt.
	 * @param attacker
	 * @param damage
	 * @return
	 */
	private int getPlayerDamage(Player attacker, int damage) {
		ItemStack weapon = attacker.getItemInHand();
		Material weaponType = weapon.getType();
		
		Integer tmpDamage = this.dm.getItemDamage(weaponType, attacker);
		
		return tmpDamage == null ? damage : tmpDamage;
	}
	
	/**
	 * Get a player's projectile damage.
	 * @param attacker
	 * @param projectile
	 * @param damage
	 * @return
	 */
	private int getPlayerProjectileDamage(Player attacker, Projectile projectile, int damage) {
		Integer tmpDamage = this.dm.getProjectileDamage(CharacterDamageManager.ProjectileType.valueOf(projectile),
				attacker);
		return tmpDamage == null ? damage : tmpDamage;
	}
	
	/**
	 * Check if the player has a resistance.
	 * @param defender
	 * @param skill
	 * @return
	 */
	private boolean resistanceCheck(Entity defender, Skill skill) {
		if (defender instanceof LivingEntity) {
			CharacterTemplate character = this.plugin.getCharacterManager().getCharacter((LivingEntity) defender);
			if (character.hasEffectType(EffectType.RESIST_FIRE) && skill.isType(SkillType.FIRE))
				return true;
			if (character.hasEffectType(EffectType.RESIST_DARK) && skill.isType(SkillType.DARK))
				return true;
			if (character.hasEffectType(EffectType.RESIST_LIGHT) && skill.isType(SkillType.LIGHT))
				return true;
			if (character.hasEffectType(EffectType.RESIST_LIGHTNING) && skill.isType(SkillType.LIGHTNING))
				return true;
			if (character.hasEffectType(EffectType.RESIST_ICE) && skill.isType(SkillType.ICE))
				return true;
		}
		return false;
	}
	
	/**
	 * Convert damage in terms of the plugin and vanilla's health.
	 * @param d
	 * @param character
	 * @return
	 */
	private int convertAlfDamage(double d, CharacterTemplate character) {
		int maxHealth = character.getMaxHealth();
		int damage = (int)(character.getEntity().getMaxHealth() / maxHealth * d);
		if (damage == 0)
			damage = 1;
		
		return damage;
	}
	
}
