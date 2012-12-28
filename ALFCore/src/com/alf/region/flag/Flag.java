package com.alf.region.flag;

/**
 * Describes a certain property of a Region.
 * @author Eteocles
 *
 * @param <T>
 */
public abstract class Flag<T> {

	private String name;
	private FlagType type;

	/**
	 * Construct the flag.
	 * @param name
	 * @param type
	 */
	public Flag(String name, FlagType type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Get the name.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the flag type.
	 * @return
	 */
	public FlagType getType() {
		return type;
	}
}
