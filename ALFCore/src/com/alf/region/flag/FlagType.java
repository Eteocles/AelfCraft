package com.alf.region.flag;

/**
 * Describes a type of flag.
 * @author Eteocles
 */
public enum FlagType {
	
	//Difficulty multiplier for spawning rates and monster difficulty.
	DIFFICULTY("difficulty"),
	//Whether a region is protected from non-member player modification.
	PROTECT("protect"),
	//Whether a region is safe for players (e.g. no damage, no hostile mob spawns).
	SAFE("safe"),
	//Whether a region is exclusively an ore vein. (incompatible with other region flags).
	OREVEIN("ore-vein"),
	//Where to teleport a player to when entering it.
	TELEPORT_TO("teleport-to"),
	//Text to display on player entry.
	ENTER_TEXT("enter-text"),
	//Text to display on player exit.
	EXIT_TEXT("exit-text"),
	//Whether to allow player entry.
	ENTRY("entry"),
	//Whether to allow player exit.
	EXIT("exit");

	//String node contained for each Flag type.
	private final String node;
	
	private FlagType(String node) {
		this.node = node;
	}
	
	public String node() {
		return this.node;
	}
	
	public String toString() {
		return this.node;
	}
	
}
