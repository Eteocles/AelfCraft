package com.alf.listener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.classes.AlfClass;
import com.alf.util.Messaging;
import com.alf.util.Properties;

/**
 * Listens to block events. Handles block tracking.
 * @author Eteocles
 */
public class ABlockListener implements Listener {
	
	private final AlfCore plugin;
	private int blockTrackingDuration = 0;
	public static Map<Location, Long> placedBlocks;
	
	public ABlockListener(AlfCore plugin) {
		this.plugin = plugin;
		init();
	}
	
	/**
	 * Initiate the listener.
	 */
	public void init() {
		final int maxTrackedBlocks = AlfCore.properties.maxTrackedBlocks;
		this.blockTrackingDuration = AlfCore.properties.blockTrackingDuration;
		placedBlocks = new LinkedHashMap<Location, Long>() {
			private static final long serialVersionUID = 5987428691058084614L;
			private final int MAX_ENTRIES = maxTrackedBlocks;
			
			protected boolean removeEldestEntry(Map.Entry<Location, Long> eldest) {
				return (size() > this.MAX_ENTRIES || eldest.getValue() + ABlockListener.this.blockTrackingDuration <= System.currentTimeMillis());
			}
		};
	}
	
	/**
	 * Handle block breaking.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		//Suppress exp dropping.
		event.setExpToDrop(0);
		Properties prop = AlfCore.properties;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		Alf alf = this.plugin.getCharacterManager().getAlf(player);
		
		double addedExp = 0.0D;
		
		AlfClass.ExperienceType et = null;
		//Load in exp quantity for mining..
		if (prop.miningExp.containsKey(block.getType())) {
			addedExp = prop.miningExp.get(block.getType());
			et = AlfClass.ExperienceType.MINING;
		}
		//Farming.
		if (prop.farmingExp.containsKey(block.getType())) {
			double newExp = prop.farmingExp.get(block.getType());
			if (newExp > addedExp) {
				addedExp = newExp;
				et = AlfClass.ExperienceType.FARMING;
			}
		}
		//Logging.
		if (prop.loggingExp.containsKey(block.getType())) {
			double newExp = prop.loggingExp.get(block.getType());
			if (newExp > addedExp) {
				addedExp = newExp;
				et = AlfClass.ExperienceType.LOGGING;
			}
		}
		
		if (addedExp == 0.0D)
			return;
		
		if (wasBlockedPlaced(block)) {
			if (alf.isVerbose() && alf.hasExperienceType(et))
				Messaging.send(player, "No experience gained - blocked placed too recently.", new Object[0]);
			placedBlocks.remove(block.getLocation());
			return;
		}
		if (alf.hasParty())
			alf.getParty().gainExp(addedExp, et, block.getLocation());
		else if (alf.canGain(et))
			alf.gainExp(addedExp, et, block.getLocation().add(0.5D, 0.0D, 0.5D));
	}
	
	/**
	 * Handle block placement.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Material material = block.getType();
		
		Properties prop = AlfCore.properties;
		
		if (prop.miningExp.containsKey(material) || prop.loggingExp.containsKey(material) || prop.farmingExp.containsKey(material)) {
			placedBlocks.put(block.getLocation().clone(), System.currentTimeMillis());
		}
	}
	
	/**
	 * Whether a block was placed by a user.
	 * @param block
	 * @return
	 */
	private boolean wasBlockedPlaced(Block block) {
		Location loc = block.getLocation();
		
		if (placedBlocks.containsKey(loc)) {
			long timedPlaced = placedBlocks.get(loc);
			if (timedPlaced + this.blockTrackingDuration > System.currentTimeMillis())
				return true;
			return false;
		}
		
		return false;
	}
	
	/**
	 * Handle piston block extensions.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		List<Location> movedBlocks = new ArrayList<Location>();
		for (Block b : event.getBlocks())
			if (placedBlocks.containsKey(b.getLocation()))
				movedBlocks.add(b.getLocation());
		
		int x = event.getDirection().getModX();
		int y = event.getDirection().getModY();
		int z = event.getDirection().getModZ();
		
		long time = System.currentTimeMillis();
		for (Location loc : movedBlocks)
			placedBlocks.put(loc.clone().add(x,y,z), time);
	}
	
	/**
	 * Handle piston block retraction.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		Location loc = event.getBlock().getLocation();
		if (placedBlocks.containsKey(loc)) {
			placedBlocks.remove(loc);
			placedBlocks.put(event.getBlock().getRelative(event.getDirection()).getLocation(), System.currentTimeMillis());
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkUnload(ChunkUnloadEvent event) {
		this.plugin.getCharacterManager().cleanupEntities(event.getChunk().getEntities());
	}
	
}
