package com.alf.region;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.alf.region.flag.Flag;

/**
 * Describes a type representing an in-world zone of an abstract shape.
 * @author Eteocles
 */
public abstract class Region {

	/**
	 * Used to determine legal ids.
	 */
	private static final Pattern idPattern = Pattern.compile("^[A-Za-z0-9_,'\\-\\+/]{1,}$");
	
	/**
	 * Label for the region.
	 */
	private String id;

	/**
	 * List of flags.
	 */
	private Map<Flag<?>, Object> flags = new HashMap<Flag<?>, Object>();
	
	/**
	 * Priority of this region vs. other regions.
	 */
	private int priority = 0;
	
	/**
	 * Construct the region.
	 * @param id
	 */
	public Region(String id) {
		this.id = id;
	}
	
	/**
	 * Whether an ID is valid.
	 * @param id
	 * @return
	 */
	public static boolean isValidId(String id) {
		return idPattern.matcher(id).matches();
	}
	
	/**
	 * Get the ID for the region.
	 * @return
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Get the priority of the region.
	 * @return
	 */
	public int getPriority() {
		return this.priority;
	}
	
	/**
	 * Set the priority of the region.
	 * @param priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	/**
	 * Get the hash code for this region.
	 */
	public int hashCode() {
		return this.id.hashCode();
	}	
	
	/**
	 * Whether the two regions are equal.
	 */
	public boolean equals(Object obj) {
		if (! (obj instanceof Region))
			return false;
		return ((Region)obj).getId().equals(id);
	}
	
	/**
	 * Set a flag.
	 * @param value
	 * @return
	 */
	public <T extends Flag<V>, V> void setFlag(T flag, V val) {
		if (val == null) 
			flags.remove(flag);
		else
			flags.put(flag, val);
	}

	/**
	 * Get the value for the flag type.
	 * @param flagType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Flag<V>, V> V getFlag(T flag) {
		Object obj = flags.get(flag);
		V val;
		if (obj != null)
			val = (V) obj;
		else
			return null;
		return val;
	}

	/**
	 * Get the map of all the flags.
	 * @return
	 */
	public Map<Flag<?>, Object> getFlags() {
		return flags;
	}

	/**
	 * Whether the region contains a given location.
	 * @param loc - location to be checked for
	 * @return - whether or not the region contains the player
	 */
	public abstract boolean containsLocation(Location loc);

	/**
	 * Whether the region contains a given player.
	 * @param p - player to be checked for
	 * @return - whether or not the region contains the player
	 */
	public abstract boolean containsPlayer(Player p);

	 /**
     * Thrown when setting a curParent would create a circular inheritance
     * situation.
     */
    public static class CircularInheritanceException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 364491337944851709L;
    }
	
}
