package com.alf.character;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;

import com.alf.AlfCore;
import com.alf.api.SkillUseInfo;
import com.alf.character.classes.AlfClass;
import com.alf.skill.Skill;
import com.alf.util.Util;

/**
 * Handles character damaging.
 * @author Eteocles
 */
public class CharacterDamageManager {

	private AlfCore plugin;
	private Map<Material, Integer> matBaseDamage;
	private Map<Material, Double> matDmgVars;
	private Map<Material, int[]> wepDmgRanges;
	private Map<Material, Integer> itemDamage;
	private Map<ProjectileType, Integer> projectileDamage;
	private Map<EntityType, Integer> creatureHealth;
	private Map<EntityType, Integer> creatureDamage;
	private Map<EntityDamageEvent.DamageCause, Double> environmentalDamage;
	private Map<Integer, SkillUseInfo> spellTargs = new HashMap<Integer, SkillUseInfo>();
	private Map<String, Double> damageBuffs = new HashMap<String, Double>();
	private double gapping;
	
	/**
	 * Constructs the damage manager.
	 * @param plugin
	 */
	public CharacterDamageManager(AlfCore plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Load information from configuration.
	 * @param config
	 */
	public void load(Configuration config) {
		AlfCore.properties.potHealthPerTier = config.getDouble("potions.health-per-tier", 0.1D);
		
		//Get creature health values.
		this.creatureHealth = new EnumMap<EntityType, Integer>(EntityType.class);
		ConfigurationSection section = config.getConfigurationSection("creature-health");
		boolean errored = false;
		
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			if (keys != null) {
				for (String key : keys) {
					try {
						EntityType type = EntityType.fromName(key);
						if (type == null)
							throw new IllegalArgumentException();
						int health = section.getInt(key, 20);
						if (health <= 0) {
							health = 20;
						}
						this.creatureHealth.put(type, health);
					} catch (IllegalArgumentException e) {
						AlfCore.log(Level.WARNING, "Invalid creature type (" + key + ") found in damages.yml.");
						errored = true;
					}
				}
			}
		}
		
		//Get creature damage values.
		this.creatureDamage = new EnumMap<EntityType, Integer>(EntityType.class);
		section = config.getConfigurationSection("creature-damage");
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			if (keys != null) {
				for (String key : keys) {
					try {
						EntityType type = EntityType.fromName(key);
						if (type == null)
							throw new IllegalArgumentException();
						this.creatureDamage.put(type, section.getInt(key, 10));
					} catch (IllegalArgumentException e) {
						AlfCore.log(Level.WARNING, "Invalid creature type (" + key + ") found in damages.yml.");
						errored = true;
					}
				}
			}
		}
		
		if (errored) {
			AlfCore.log(Level.WARNING, "Remember, creature-names are case-sensitive, and must be exactly the same as found in the defaults!");
		}
		
		//Get material base damage. Should only contain types of weapon mats.
		this.matBaseDamage = new EnumMap<Material, Integer>(Material.class);
		section = config.getConfigurationSection("material-base-damage");
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			if (keys != null) {
				for (String key : keys) {
					try {
						switch (key) {
						case "WOOD":
							for (String s : Util.woodWeps)
								this.matBaseDamage.put(Material.matchMaterial(s), section.getInt(key));
							break;
						case "GOLD":
							for (String s : Util.goldWeps)
								this.matBaseDamage.put(Material.matchMaterial(s), section.getInt(key));
							break;
						case "IRON":
							for (String s : Util.ironWeps)
								this.matBaseDamage.put(Material.matchMaterial(s), section.getInt(key));
							break;
						case "DIAMOND":
							for (String s : Util.diamondWeps)
								this.matBaseDamage.put(Material.matchMaterial(s), section.getInt(key));
							break;
						default:
							throw new IllegalArgumentException();
						}
					} catch (IllegalArgumentException e) {
						AlfCore.log(Level.WARNING, "Invalid material weapon type (" + key +") found in damages.yml.");
					}
				}
			}
		}
		
		//Get material damage variance.
		this.matDmgVars = new EnumMap<Material, Double>(Material.class);
		section = config.getConfigurationSection("material-damage-variance");
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			if (keys != null) {
				for (String key : keys) {
					try {
						switch (key) {
						case "WOOD":
							for (String s : Util.woodWeps)
								this.matDmgVars.put(Material.matchMaterial(s), section.getDouble(key));
							break;
						case "GOLD":
							for (String s : Util.goldWeps)
								this.matDmgVars.put(Material.matchMaterial(s), section.getDouble(key));
							break;
						case "IRON":
							for (String s : Util.ironWeps)
								this.matDmgVars.put(Material.matchMaterial(s), section.getDouble(key));
							break;
						case "DIAMOND":
							for (String s : Util.diamondWeps)
								this.matDmgVars.put(Material.matchMaterial(s), section.getDouble(key));
							break;
						default:
							throw new IllegalArgumentException();
						}
					} catch (IllegalArgumentException e) {
						AlfCore.log(Level.WARNING, "Invalid material weapon type (" + key +") found in damages.yml.");
					}
				}
			}
		}
		
		gapping = config.getDouble("gapping");
		
		this.loadItemDamageRanges();
		
		//Load specific item damage.
		this.itemDamage = new EnumMap<Material, Integer> (Material.class);
		section = config.getConfigurationSection("item-damage");
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			if (keys != null) {
				for (String key : keys) {
					try {
						Material item = Material.matchMaterial(key);
						if (item == null)
							throw new IllegalArgumentException();
						this.itemDamage.put(item, section.getInt(key, 2));
					} catch (IllegalArgumentException e) {
						AlfCore.log(Level.WARNING, "Invalid item type (" + key + ") found in damages.yml.");
					}
				}
			}
		}
		
		//Load specific environmental damage.
		this.environmentalDamage = new EnumMap<EntityDamageEvent.DamageCause, Double>(EntityDamageEvent.DamageCause.class);
		section = config.getConfigurationSection("environmental-damage");
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			if (keys != null) {
				for (String key : keys) {
					try {
						EntityDamageEvent.DamageCause cause = EntityDamageEvent.DamageCause.valueOf(key.toUpperCase());
						if (cause == null)
							throw new IllegalArgumentException();
						double damage = section.getDouble(key, 0.0D);
						this.environmentalDamage.put(cause, damage);
					} catch (IllegalArgumentException e) {
						AlfCore.log(Level.WARNING, 
								"Invalid environmental damage type (" + key + ") found in damages.yml.");
					}
				}
			}
		}
		
		//Load projectile damage.
		this.projectileDamage = new EnumMap<ProjectileType, Integer>(ProjectileType.class);
		section = config.getConfigurationSection("projectile-damage");
		if (section != null) {
			Set<String> keys = section.getKeys(false);
			if (keys != null)
				for (String key : keys) {
					ProjectileType type = ProjectileType.valueOf(key.toUpperCase());
					if (type != null)
						this.projectileDamage.put(type,  section.getInt(key, 0));
				}
		}
	}
	
	/**
	 * Load the item damage ranges.
	 */
	private void loadItemDamageRanges() {
		this.wepDmgRanges = new HashMap<Material, int[]>();
		//Damage range factor = damageVal * percentRange^(damageVal)^(1/expGap)
		for (Material mat : this.matBaseDamage.keySet()) {
			int damageVal = this.matBaseDamage.get(mat);
			double percentRange = this.matDmgVars.get(mat);
			double factor = damageVal*Math.pow(percentRange, damageVal/gapping);
//			AlfCore.log(Level.INFO, "Damage Calc | " + mat.name() + ": " + (int)Math.floor(damageVal - factor) + ", " + (int)Math.ceil(damageVal + factor));
			this.wepDmgRanges.put(mat, new int[] {(int)Math.floor(damageVal - factor), (int)Math.ceil(damageVal + factor)});
		}
	}
	
	/**
	 * Get the damage ranges for a given weapon.
	 * @param weapon
	 * @param level
	 * @return
	 */
	public int[] getWeaponDamage(Material weapon, int level) {
		int[] damageRange = this.wepDmgRanges.get(weapon);
		if (damageRange != null) {
			for (int i = 0; i < damageRange.length; i++)
				damageRange[i] += (int)(((double)damageRange[i])*((double)level)*0.10D);
			return damageRange;
		} else {
			return null;
		}
	}
	
	/**
	 * Get the amount of damage dealt by a projectile type from an entity.
	 * @param type
	 * @param entity
	 * @return
	 */
	public Integer getProjectileDamage(ProjectileType type, HumanEntity entity) {
		
		if (entity != null && entity instanceof Player) {
			Alf alf = this.plugin.getCharacterManager().getAlf((Player) entity);
			AlfClass alfClass = alf.getAlfClass();
			AlfClass secondClass = alf.getSecondClass();
			Integer classDamage = alfClass.getProjectileDamage(type);
			
			if (classDamage != null)
				classDamage += (int) (alfClass.getProjDamageLevel(type) * alf.getLevel(alfClass));
			
			Integer secondDamage = null;
			if (secondClass != null) {
				secondDamage = secondClass.getProjectileDamage(type);
				if (secondDamage != null)
					secondDamage += (int)(secondClass.getProjDamageLevel(type) * alf.getLevel(secondClass));
			}

			if (classDamage != null && secondDamage != null)
				return (classDamage > secondDamage) ? classDamage : secondDamage;
			
			if (classDamage != null)
				return classDamage;
			
			if (secondDamage != null)
				return secondDamage;
		}
		return (Integer) this.projectileDamage.get(type);
	}
	
	/**
	 * Get the amount of damage dealt by using an item.
	 * @param item
	 * @param entity
	 * @return
	 */
	public Integer getItemDamage(Material item, HumanEntity entity) {
		if (entity != null && entity instanceof Player) {
			Alf alf = this.plugin.getCharacterManager().getAlf((Player) entity);
			
			if (this.wepDmgRanges.containsKey(item)) {
				int damageRange[] = this.wepDmgRanges.get(item);
				//Randomly choose a number from within the damage range.
				double damage = damageRange[0] + (int)(Math.random()*(damageRange[1] - damageRange[0]));
				return (int)damage;
			}
			
			AlfClass alfClass = alf.getAlfClass();
			AlfClass secondClass = alf.getSecondClass();
			Integer classDamage = alfClass.getItemDamage(item);
			if (classDamage != null)
				classDamage += (int) (alfClass.getItemDamageLevel(item) * alf.getLevel(alfClass));
			
			Integer secondDamage = null;
			if (secondClass != null) {
				secondDamage = secondClass.getItemDamage(item);
				if (secondDamage != null)
					secondDamage += (int)(secondClass.getItemDamageLevel(item) * alf.getLevel(secondClass));
			}
			
			if (classDamage != null && secondDamage != null)
				return (classDamage > secondDamage) ? classDamage : secondDamage;
			
			if (classDamage != null)
				return classDamage;
			
			if (secondDamage != null)
				return secondDamage;
		}
		
		return (Integer) this.itemDamage.get(item);
	}
	
	/**
	 * Get the environmental damage dealt from a cause.
	 * @param cause
	 * @return
	 */
	public Double getEnvironmentalDamage(EntityDamageEvent.DamageCause cause) {
		return this.environmentalDamage.get(cause);
	}
	
	/**
	 * Get the maximum health value for the entity.
	 * @param lEntity
	 * @return
	 */
	public int getMaxHealth(LivingEntity lEntity) {
		return this.plugin.getCharacterManager().getMaxHealth(lEntity);
	}
	
	/**
	 * Get the health of the entity.
	 * @param lEntity
	 * @return
	 */
	public int getHealth(LivingEntity lEntity) {
		return this.plugin.getCharacterManager().getHealth(lEntity);
	}
	
	/**
	 * Get the entity's maximum health.
	 * @param lEntity
	 * @return
	 */
	protected int getEntityMaxHealth(LivingEntity lEntity) {
		Integer val = (Integer) this.creatureHealth.get(lEntity.getType());
		return val != null ? val : lEntity.getMaxHealth();
	}
	
	/**
	 * Get the entity's damage dealt.
	 * @param type
	 * @return
	 */
	protected int getEntityDamage(EntityType type) {
		Integer val = (Integer) this.creatureDamage.get(type);
		return val != null ? val : 1;
	}

	/**
	 * Whether the entity is a spell target.
	 * @param o
	 * @return
	 */
	public boolean isSpellTarget(Entity o) {
		return this.spellTargs.containsKey(o.getEntityId());
	}
	
	/**
	 * Get the information regarding the targeter of this entity.
	 * @param o
	 * @return
	 */
	public SkillUseInfo getSpellTargetInfo(Entity o) {
		return (SkillUseInfo) this.spellTargs.get(o.getEntityId());
	}
	
	/**
	 * Remove the entity as a spell target.
	 * @param o
	 * @return
	 */
	public SkillUseInfo removeSpellTarget(Entity o) {
		return (SkillUseInfo) this.spellTargs.remove(o.getEntityId());
	}
	
	/**
	 * Add an entity as a target of a character's skill.
	 * @param o
	 * @param character
	 * @param skill
	 */
	public void addSpellTarget(Entity o, CharacterTemplate character, Skill skill) {
		SkillUseInfo skillInfo = new SkillUseInfo(character, skill);
		this.spellTargs.put(o.getEntityId(), skillInfo);
	}
	
	/**
	 * Add an alf damage buff.
	 * @param player
	 * @param buff
	 */
	public void addAlfDamageBuff(Player player, double buff) {
		Double damageBuff = this.damageBuffs.get(player.getName());
		if (damageBuff == null || damageBuff < buff)
			this.damageBuffs.put(player.getName(), buff);
	}
	
	/**
	 * Get the Alf's damage buff.
	 * @param player
	 * @return
	 */
	public double getAlfDamageBuff(Player player) {
		Double damageBuff = this.damageBuffs.get(player.getName());
		return (damageBuff == null) ? 0.0D : damageBuff;
	}
	
	/**
	 * Remove a player's damage buff.
	 * @param player
	 */
	public void removeAlfDamageBuff(Player player) {
		this.damageBuffs.remove(player.getName());
	}
	
	/**
	 * Describes a type of Minecraft projectile.
	 * @author Eteocles
	 */
	public static enum ProjectileType {
		ARROW, 
		EGG, 
		SNOWBALL;

		/**
		 * Matches a projectile to the given string.
		 * @param name
		 * @return
		 */
		public static ProjectileType matchProjectile(String name) {
			if (name.equalsIgnoreCase("arrow"))
				return ARROW;
			if (name.equalsIgnoreCase("snowball"))
				return SNOWBALL;
			if (name.equalsIgnoreCase("egg"))
				return EGG;
			return null;
		}

		public static ProjectileType valueOf(Entity entity) {
			if ((entity instanceof Arrow))
				return ARROW;
			if ((entity instanceof Snowball))
				return SNOWBALL;
			if ((entity instanceof Egg))
				return EGG;
			return null;
		}
	}

}
