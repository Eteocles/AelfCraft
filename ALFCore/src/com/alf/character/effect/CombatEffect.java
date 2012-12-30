package com.alf.character.effect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.alf.AlfCore;
import com.alf.api.event.AlfLeaveCombatEvent;
import com.alf.character.Alf;
import com.alf.character.Monster;
import com.alf.util.Messaging;
import com.alf.util.Properties;

/**
 * Applied when a player enters combat.
 * @author Eteocles
 */
public class CombatEffect extends PeriodicEffect {

	private final Map<LivingEntity, CombatReason> combatMap = new HashMap<LivingEntity, CombatReason>();
	private LivingEntity lastCombatEntity = null;
	
	/**
	 * Reason for leaving combat.
	 * @author Eteocles
	 */
	public static enum LeaveCombatReason {
		DEATH,
		SUICIDE,
		ERROR,
		TIMED,
		LOGOUT,
		TARGET_DEATH,
		TARGET_LOGOUT,
		CUSTOM;
	}
	
	/**
	 * Reason for entering combat.
	 * @author Eteocles
	 */
	public static enum CombatReason {
		DAMAGED_BY_MOB,
		DAMAGED_BY_PLAYER,
		ATTACKED_MOB,
		ATTACKED_PLAYER,
		CUSTOM;
	}

	/**
	 * Construct the Combat Effect.
	 * @param plugin
	 */
	public CombatEffect(AlfCore plugin) {
		super(plugin, "Combat", Properties.combatTime);
	}
	
	/**
	 * Tick the alf.
	 * The combat effect ticks only when it wears off. Guaranteed to have timed out.
	 */
	public final void tickAlf(Alf alf) {
		super.tickAlf(alf);
		if(! this.combatMap.isEmpty()) {
			this.combatMap.clear();
			Bukkit.getServer().getPluginManager().callEvent(
					new AlfLeaveCombatEvent(alf, this.combatMap, LeaveCombatReason.TIMED));
			Messaging.send(alf.getPlayer(), "You have left combat!", new Object[0]);
		}
	}
	
	/**
	 * Do nothing.
	 */
	public final void tickMonster() {}
	
	public final void applyToAlf(Alf alf) {}
	
	public final void applyToMonster(Monster monster) {}

	/**
	 * Whether the affected entity is in combat.
	 * @return
	 */
	public boolean isInCombat() {
		if (this.combatMap.isEmpty())
			return false;
		if (this.applyTime + getPeriod() < System.currentTimeMillis()) {
			this.combatMap.clear();
			return false;
		}
		return true;
	}

	/**
	 * Whether the the given entity is part of the combat.
	 * @param target
	 * @return
	 */
	public boolean isInCombatWith(LivingEntity target) {
		return this.combatMap.containsKey(target);
	}

	/**
	 * Enter combat with a given entity.
	 * @param entity
	 * @param reason
	 */
	public final void enterCombatWith(LivingEntity target, CombatReason reason) {
		this.combatMap.put(target, reason);
		this.lastCombatEntity = target;
		reset();
	}

	/**
	 * Reset the Combat Effect.
	 */
	public void reset() {
		this.applyTime = System.currentTimeMillis();
		this.lastTickTime = this.applyTime;
	}

	/**
	 * Leave combat with the given target.
	 * @param alf
	 * @param target
	 * @param reason
	 */
	public void leaveCombatWith(Alf alf, LivingEntity target,
			LeaveCombatReason reason) {
		if (this.combatMap.remove(target) != null && this.combatMap.isEmpty()) {
			this.lastCombatEntity = target;
			alf.leaveCombatWith(target, reason);
		}
	}

	/**
	 * Leave combat from logout.
	 * @param alf
	 */
	public void leaveCombatFromLogout(Alf alf) {
		Bukkit.getServer().getPluginManager().callEvent(
				new AlfLeaveCombatEvent(alf, this.combatMap, LeaveCombatReason.LOGOUT));
		for (LivingEntity le : new ArrayList<LivingEntity>(this.combatMap.keySet()))
			if (le instanceof Player)
				this.plugin.getCharacterManager().getAlf((Player) le).leaveCombatWith(
						alf.getPlayer(), LeaveCombatReason.TARGET_LOGOUT
		);
		this.combatMap.clear();
	}

	/**
	 * Leave combat from death.
	 * @param alf
	 */
	public void leaveCombatFromDeath(Alf alf) {
		Bukkit.getServer().getPluginManager().callEvent(
				new AlfLeaveCombatEvent(alf, this.combatMap, LeaveCombatReason.DEATH));
		for (LivingEntity le : new ArrayList<LivingEntity>(this.combatMap.keySet()))
			if (le instanceof Player)
				this.plugin.getCharacterManager().getAlf((Player) le).leaveCombatWith(
						alf.getPlayer(), LeaveCombatReason.TARGET_DEATH
		);
		this.combatMap.clear();
	}

	/**
	 * Leave combat from suicide.
	 * @param alf
	 */
	public void leaveCombatFromSuicide(Alf alf) {
		Bukkit.getServer().getPluginManager().callEvent(
				new AlfLeaveCombatEvent(alf, this.combatMap, LeaveCombatReason.SUICIDE));
		for (LivingEntity le : new ArrayList<LivingEntity>(this.combatMap.keySet()))
			if (le instanceof Player)
				this.plugin.getCharacterManager().getAlf((Player) le).leaveCombatWith(
						alf.getPlayer(), LeaveCombatReason.TARGET_LOGOUT
		);
		this.combatMap.clear();
	}

	/**
	 * Get the map of total combatants.
	 * @return
	 */
	public Map<LivingEntity, CombatReason> getCombatants() {
		return Collections.unmodifiableMap(this.combatMap);
	}
	
	/**
	 * Get the last combatant.
	 * @return
	 */
	public final LivingEntity getLastCombatant() {
		return this.lastCombatEntity;
	}
	
}
