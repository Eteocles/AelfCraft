package com.alf.character;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import com.alf.AlfCore;
import com.alf.api.event.AlfLeavePartyEvent;
import com.alf.character.classes.AlfClass;
import com.alf.character.party.AlfParty;
import com.alf.command.CommandHandler;
import com.alf.persistence.AlfStorage;
import com.alf.persistence.YMLAlfStorage;
import com.alf.skill.DelayedSkill;
import com.alf.skill.OutsourcedSkill;
import com.alf.skill.PassiveSkill;
import com.alf.skill.Skill;

/**
 * Manages all characters in the server.
 * @author Eteocles
 */
public class CharacterManager {

	//Reference to main plugin core.
	private AlfCore plugin;
	//Set of Alfs.
	private Map<String, Alf> alfs;
	//Handles Alf storage.
	private AlfStorage alfStorage;
	//Interval for mana-ticks.
	private static final int manaInterval = 5;
	//Interval for delayed skill ticks.
	private static final int warmupInterval = 5;
	//Set of Alfs and their delayed skills.
	private Map<Alf, DelayedSkill> delayedSkills;
	//Queue of all entities.
	private ConcurrentLinkedQueue<Entity> entityQueue;
	private List<Alf> completedSkills;
	private Map<UUID, Monster> monsters;
	private Map<UUID, Pet> pets;
	private int taskId = 0;

	/**
	 * Construct the CharacterManager.
	 * @param alfCore
	 */
	public CharacterManager(AlfCore alfCore) {
		this.plugin = alfCore;
		this.alfs = new HashMap<String, Alf>();
		this.entityQueue = new ConcurrentLinkedQueue<Entity>();
		this.monsters = new ConcurrentHashMap<UUID, Monster>();
		this.pets = new ConcurrentHashMap<UUID, Pet>();
		this.taskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
				plugin, new EntityReaper(), 100L, 100L).getTaskId();

		this.alfStorage = new YMLAlfStorage(plugin);

		long regenInterval = AlfCore.properties.manaRegenInterval * 1000L;
		Runnable manaTimer = new ManaUpdater(this, regenInterval);
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, manaTimer, 0L, manaInterval);

		this.delayedSkills = new HashMap<Alf, DelayedSkill>();
		this.completedSkills = new ArrayList<Alf>();
		Runnable delayedExecuter = new DelayedSkillExecuter(this);
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, delayedExecuter, 0L, warmupInterval);
	}

	/**
	 * Stop all timers.
	 */
	public void stopTimers() {
		this.plugin.getServer().getScheduler().cancelTasks(this.plugin);
	}

	/**
	 * Run on shut down.
	 */
	public void shutdown() {
		this.alfStorage.shutdown();
		for (Pet p : pets.values())
			p.getEntity().remove();
		Bukkit.getScheduler().cancelTask(this.taskId);
	}

	/**
	 * Add an alf to the manager.
	 * @param alf
	 * @return
	 */
	public Alf addAlf(Alf alf) {
		this.alfs.put(alf.getPlayer().getName().toLowerCase(), alf);
		return alf;
	}

	/**
	 * Add a monster to the manager.
	 * @param monster
	 * @return
	 */
	public boolean addMonster(Monster monster) {
		UUID id = monster.getEntity().getUniqueId();
		if (this.monsters.containsKey(id))
			return false;
		this.monsters.put(id, monster);
		return true;
	}
	
	/**
	 * Add a pet to the manager.
	 * @param pet
	 * @return
	 */
	public boolean addPet(Pet pet) {
		UUID id = pet.getEntity().getUniqueId();
		if (this.pets.containsKey(id))
			return false;
		this.pets.put(id, pet);
		return true;
	}

	/**
	 * Get the encapsulating character type.
	 * @param lEntity
	 * @return
	 */
	public CharacterTemplate getCharacter(LivingEntity lEntity) {
		if (lEntity instanceof Player) 
			return getAlf((Player) lEntity);
		else if (this.pets.containsKey(lEntity.getUniqueId()))
			return getPet(lEntity);
		return getMonster(lEntity);
	}
	
	/**
	 * Get the encapsulating pet.
	 * @param lEntity
	 * @return
	 */
	public Pet getPet(LivingEntity lEntity) {
		return this.pets.get(lEntity.getUniqueId());
	}

	/**
	 * Get the monster encapsulating the entity.
	 * @param lEntity
	 * @return
	 */
	public Monster getMonster(LivingEntity lEntity) {
		UUID id = lEntity.getUniqueId();
		if (this.monsters.containsKey(id))
			return getMonster(id);
		Monster monster = new Monster(this.plugin, lEntity);
		this.monsters.put(id, monster);
		return monster;
	}

	/**
	 * Get a monster by its UUID.
	 * @param id
	 * @return
	 */
	public Monster getMonster(UUID id) {
		return (Monster) this.monsters.get(id);
	}

	/**
	 * Whether a monster of the same id is entered in the manager.
	 * @param lEntity
	 * @return
	 */
	public boolean isMonsterSetup(LivingEntity lEntity) {
		return this.monsters.containsKey(lEntity.getUniqueId());
	}

	/**
	 * Get the Alf encapsulating a player.
	 * @param p
	 * @return
	 */
	public Alf getAlf(Player p) {
		AlfCore.debug.startTask("AlfManager.getAlf");
		String key = p.getName().toLowerCase();
		Alf alf = (Alf) this.alfs.get(key);
		if (alf != null) {
			if (alf.getPlayer().getEntityId() != p.getEntityId()) {
				AlfCore.log(Level.SEVERE, "Alf with invalid entity ID detected and reloaded. Check for exploits!");
				alf.clearEffects();
				alf.setPlayer(p);
				performSkillChecks(alf);
				AlfCore.debug.stopTask("AlfManager.getAlf");
				return alf;
			}
			AlfCore.debug.stopTask("AlfManager.getAlf");
			return alf;
		}
		alf = this.alfStorage.loadAlf(p);
		addAlf(alf);
		performSkillChecks(alf);
		AlfCore.debug.stopTask("AlfManager.getAlf");
		return alf;
	}

	/**
	 * Check all Alf classes for permissions, etcl.
	 * @param alf
	 */
	public void checkClass(Alf alf) {
		Player p = alf.getPlayer();
		AlfClass playerClass = alf.getAlfClass();
		AlfClass secondClass = alf.getSecondClass();
		//If player doesn't have permissions...
		if (! CommandHandler.hasPermission(p, "alf.classes."+playerClass.getName().toLowerCase()))
			alf.setAlfClass(this.plugin.getClassManager().getDefaultClass(), false);
		if (secondClass != null && ! CommandHandler.hasPermission(p, "alf.classes."+secondClass.getName().toLowerCase()))
			alf.setAlfClass(null, true);
	}

	/**
	 * Perform all skill checks for his Alf.
	 * @param alf
	 */
	public void performSkillChecks(Alf alf) {
		for (Skill skill : this.plugin.getSkillManager().getSkills()) {
			if (skill instanceof OutsourcedSkill) 
				((OutsourcedSkill)skill).tryLearningSkill(alf);
			else if (skill instanceof PassiveSkill)
				((PassiveSkill)skill).tryApplying(alf);
		}
	}

	/**
	 * Remove an Alf from the manager.
	 * @param alf
	 */
	public void removeAlf(Alf alf) {
		if (alf != null) {
			if (alf.hasParty()) {
				AlfParty party = alf.getParty();

				AlfLeavePartyEvent event = new AlfLeavePartyEvent(alf, party, AlfLeavePartyEvent.LeavePartyReason.DISCONNECT);
				this.plugin.getServer().getPluginManager().callEvent(event);
				party.removeMember(alf);
				if (party.getMembers().size() == 0)
					this.plugin.getPartyManager().removeParty(party);
			}
		}
		this.completedSkills.remove(alf);
		this.delayedSkills.remove(alf);
		this.alfs.remove(alf.getName().toLowerCase());
	}

	/**
	 * Remove the given entity from the server if it exists.
	 * @param lEntity
	 */
	public void removeMonster(LivingEntity lEntity) {
		if (this.monsters.containsKey(lEntity.getUniqueId())) {
			Monster monster = (Monster) this.monsters.remove(lEntity.getUniqueId());
			monster.clearEffects();
			monster.getEntity().remove();
		}
	}
	
	/**
	 * Remove the pet.
	 * @param pet
	 */
	public void removePet(Pet pet) {
		if (this.pets.containsKey(pet.getEntity().getUniqueId())) {
			this.pets.remove(pet);
			pet.getEntity().remove();
		}
	}

	/**
	 * Save an Alf in the storage.
	 * @param alf
	 * @param now
	 */
	public void saveAlf(Alf alf, boolean now) {
		this.alfStorage.saveAlf(alf, now);
		AlfCore.log(Level.INFO, "Saved alf in core: " + alf.getPlayer().getName());
	}

	/**
	 * Save an Player in the storage.
	 * @param player
	 * @param now
	 */
	public void saveAlf(Player player, boolean now) {
		saveAlf(getAlf(player), now);
	}

	/**
	 * Get an Entity's max health.
	 * @param lEntity
	 * @return
	 */
	public int getMaxHealth(LivingEntity lEntity) {
		if (lEntity instanceof Player)
			return getAlf((Player)lEntity).getMaxHealth();
		if (this.monsters.containsKey(lEntity.getUniqueId()))
			return ((Monster)this.monsters.get(lEntity.getUniqueId())).getMaxHealth();
		int maxHP = this.plugin.getDamageManager().getEntityMaxHealth(lEntity);
		if (lEntity instanceof Slime) {

		}
		return maxHP;
	}

	/**
	 * Get an Entity's health stat.
	 * @param lEntity
	 * @return
	 */
	public int getHealth(LivingEntity lEntity) {
		if (lEntity instanceof Player)
			return getAlf((Player)lEntity).getHealth();
		if (this.monsters.containsKey(lEntity.getUniqueId()))
			return ((Monster)this.monsters.get(lEntity.getUniqueId())).getHealth();
		return getMaxHealth(lEntity);
	}

	/**
	 * Get the total collection of managed Alfs.
	 * @return
	 */
	public Collection<Alf> getAlfs() {
		return Collections.unmodifiableCollection(this.alfs.values());
	}

	/**
	 * Get a collection of the stored Alfs and their delayed skills.
	 * @return
	 */
	public Map<Alf, DelayedSkill> getDelayedSkills() {
		return this.delayedSkills;
	}

	/**
	 * Get a list of Alfs who have completed skills.
	 * @return
	 */
	public List<Alf> getCompletedSkills() {
		return this.completedSkills;
	}

	/**
	 * Add an alf to the completed skills list.
	 * @param alf
	 */
	public void addCompletedSkill(Alf alf) {
		this.completedSkills.add(alf);
	}

	/**
	 * Get the Alf storage.
	 * @return
	 */
	public AlfStorage getAlfStorage() {
		return this.alfStorage;
	}

	/**
	 * Set the Alf storage.
	 * @param alfStorage
	 */
	public void setAlfStorage(AlfStorage alfStorage) {
		this.alfStorage = alfStorage;
	}

	/**
	 * Clean up all of the provided entities.
	 * @param entities
	 */
	public void cleanupEntities(Entity[] entities) {
		this.entityQueue.addAll(Arrays.asList(entities));
	}

	/**
	 * Runs periodically and reaps queued in Entities.	
	 * @author Eteocles
	 */
	class EntityReaper implements Runnable {

		EntityReaper() {}

		/**
		 * 
		 */
		public void run() {
			while (CharacterManager.this.entityQueue.peek() != null) {
				Entity entity = (Entity) CharacterManager.this.entityQueue.poll();
				if (entity instanceof LivingEntity && CharacterManager.this.monsters.containsKey(entity.getUniqueId()))
					CharacterManager.this.monsters.remove(entity.getUniqueId());
			}
		}
	}

}
