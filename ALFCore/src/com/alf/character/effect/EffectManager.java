package com.alf.character.effect;

import java.util.*;
import java.util.logging.Level;

import com.alf.AlfCore;
import com.alf.character.CharacterTemplate;

/**
 * Manages effects for AelfCraft.
 * @author Eteocles
 */
public class EffectManager {

	private Set<ManagedEffect> managedEffects = new HashSet<ManagedEffect>();
	private Set<ManagedEffect> pendingRemovals = new HashSet<ManagedEffect>();
	private Set<ManagedEffect> pendingAdditions = new HashSet<ManagedEffect>();
	private static final int effectInterval = 2;
	
	/**
	 * Construct the EffectManager.
	 * @param plugin
	 */
	public EffectManager(AlfCore plugin) {
		Runnable effectTimer = new EffectUpdater();
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, effectTimer, 0L, 
				effectInterval);
	}
	
	/**
	 * Manage the given effect by encapsulating and storing it.
	 * @param character
	 * @param effect
	 */
	public void manageEffect(CharacterTemplate character, Effect effect) {
		if (effect instanceof Expirable || effect instanceof Periodic)
			this.pendingAdditions.add(new ManagedEffect(character, effect));
	}
	
	/**
	 * Remove the bound managed effect from the manager.
	 * @param character
	 * @param effect
	 */
	public void queueForRemoval(CharacterTemplate character, Effect effect) {
		ManagedEffect mEffect = new ManagedEffect(character, effect);
		if (this.managedEffects.contains(mEffect))
			this.pendingRemovals.add(mEffect);
	}
	
	/**
	 * Updates all effects.
	 * @author Eteocles
	 */
	class EffectUpdater implements Runnable {
		EffectUpdater() {}
		
		public void run() {
			AlfCore.debug.startTask("EffectUpdater.run");
			
			//Remove all queued effects.
			Set<ManagedEffect> removals = new HashSet<ManagedEffect>(EffectManager.this.pendingRemovals);
			EffectManager.this.pendingRemovals.clear();
			for (ManagedEffect m : removals)
				EffectManager.this.managedEffects.remove(m);
			//Add all queued effects.
			Set<ManagedEffect> additions = new HashSet<ManagedEffect>(EffectManager.this.pendingAdditions);
			EffectManager.this.pendingAdditions.clear();
			for (ManagedEffect m : additions)
				EffectManager.this.managedEffects.add(m);
			//Handle periodic/expirable effects.
			for (ManagedEffect managed : EffectManager.this.managedEffects) {
				//If effect has expired, remove.
				if (managed.effect instanceof Expirable && ((Expirable) managed.effect).isExpired()) {
					try {
						managed.character.removeEffect(managed.effect);
					} catch (Exception e) {
						AlfCore.log(Level.SEVERE, 
								"There was an error attempting to remove effect: " + managed.effect.getName());
						e.printStackTrace();
					}
				}
				//If effect is periodic and ready, tick.
				if (managed.effect instanceof Periodic) {
					Periodic periodic = (Periodic) managed.effect;
					try {
						if (periodic.isReady())
							periodic.tick(managed.character);
					} catch (Exception e) {
						AlfCore.log(Level.SEVERE,
								"There was an error attempting to tick effect: " + managed.effect.getName());
						e.printStackTrace();
					}
				}
			}
			
			AlfCore.debug.stopTask("EffectUpdater.run");
		}
	}

}
