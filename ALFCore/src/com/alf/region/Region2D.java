package com.alf.region;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Describes a simple two dimensional region which spans all of the z axis.
 * @author Eteocles
 */
public class Region2D extends Region {

	private int minX = -1;
	private int minZ = -1;
	private int maxX = -1;
	private int maxZ = -1;
	
	/**
	 * Construct the 2D rectangular region defined by two opposite corner points.
	 * @param pos1 - first positioned
	 * @param pos2 - second position
	 */
	public Region2D(String id, Location pos1, Location pos2) {
		super(id);
		
		if (pos1.getBlockX() < pos2.getBlockX()) {
			this.minX = pos1.getBlockX();
			this.maxX = pos2.getBlockX();
		} else {
			this.minX = pos2.getBlockX();
			this.maxX = pos1.getBlockX();
		}
		
		if (pos1.getBlockZ() < pos2.getBlockZ()) {
			this.minZ = pos1.getBlockZ();
			this.maxZ = pos2.getBlockZ();
		} else {
			this.minZ = pos2.getBlockZ();
			this.maxZ = pos1.getBlockZ();
		}
	}

	/**
	 * Whether the region contains a given location.
	 */
	@Override
	public boolean containsLocation(Location loc) {
		return (minX <= loc.getBlockX() && loc.getBlockX() <= maxX) && (minZ <= loc.getBlockZ() && loc.getBlockZ() <= maxZ);
	}

	/**
	 * Whether the region contains a given player.
	 */
	@Override
	public boolean containsPlayer(Player p) {
		return containsLocation(p.getLocation());
	}
	


}
