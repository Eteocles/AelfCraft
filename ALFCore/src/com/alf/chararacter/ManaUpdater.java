package com.alf.chararacter;

import java.util.Collection;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.alf.AlfCore;
import com.alf.api.event.AlfRegainManaEvent;
import com.alf.util.Messaging;

/**
 * Updates online players' mana.
 * @author Eteocles
 */
public class ManaUpdater implements Runnable {

	private final CharacterManager manager;
	private final long updateInterval;
	private long lastUpdate = 0L;
	
	/**
	 * Construct the updater.
	 * @param manager
	 * @param updateInterval
	 */
	public ManaUpdater(CharacterManager manager, long updateInterval) {
		this.manager = manager;
		this.updateInterval = updateInterval;
	}

	/**
	 * Update player mana values.
	 */
	public void run() {
		AlfCore.debug.startTask("ManaUpdater.run");
		long time = System.currentTimeMillis();
		if (time < this.lastUpdate + this.updateInterval) {
			AlfCore.debug.stopTask("ManaUpdater.run");
			return;
		}
		this.lastUpdate = time;
		Collection<Alf> alfes = this.manager.getAlfs();
		for (Alf alf : alfes)
			if (alf != null && alf.getPlayer().isOnline()) {
				int regen = alf.getManaRegen();
				int mana = alf.getMana();
				if (mana < alf.getMaxMana()) {
					AlfRegainManaEvent hrmEvent = new AlfRegainManaEvent(alf, regen, null);
					Bukkit.getServer().getPluginManager().callEvent(hrmEvent);
					if (! hrmEvent.isCancelled()) {
						alf.setMana(mana + hrmEvent.getAmount());
						if (alf.isVerbose()) {
							AlfCore.log(Level.INFO, "Updating Mana Display.");
							Messaging.send(alf.getPlayer(), ChatColor.BLUE + "MANA " + Messaging.createManaBar(alf.getMana(), alf.getMaxMana()), new Object[0]);
						}
					}
				}
			}
		AlfCore.debug.stopTask("ManaUpdater.run");
	}

}
