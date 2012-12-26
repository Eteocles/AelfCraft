package com.alf.util;

/**
 * Describes a type with a universal string node.
 * @author Eteocles
 */
public enum Setting {
	AMOUNT("amount"), 
	APPLY_TEXT("apply-text"), 
	CHANCE("chance"), 
	CHANCE_LEVEL("chance-per-level"), 
	COOLDOWN("cooldown"), 
	COOLDOWN_REDUCE("cooldown-reduce"), 
	DAMAGE("damage"), 
	DAMAGE_INCREASE("damage-per-level"), 
	DAMAGE_TICK("tick-damage"), 
	DELAY("delay"), 
	DURATION("duration"), 
	DURATION_INCREASE("duration-per-level"), 
	EXP("exp"), 
	EXPIRE_TEXT("expire-text"), 
	HEALTH("health"), 
	HEALTH_INCREASE("health-per-level"), 
	HEALTH_TICK("tick-health"), 
	HEALTH_COST("health-cost"), 
	HEALTH_COST_REDUCE("health-cost-reduce"), 
	LEVEL("level"), 
	MANA("mana"), 
	MANA_REDUCE("mana-reduce"), 
	MAX_DISTANCE("max-distance"), 
	MAX_DISTANCE_INCREASE("max-distance-increase"), 
	NO_COMBAT_USE("no-combat-use"), 
	PERIOD("period"), 
	RADIUS("radius"), 
	RADIUS_INCREASE("radius-increase"), 
	REAGENT("reagent"), 
	REAGENT_COST("reagent-cost"), 
	STAMINA("stamina"), 
	STAMINA_REDUCE("stamina-reduce"), 
	UNAPPLY_TEXT("unapply-text"), 
	USE_TEXT("use-text"), 
	DEATH_TEXT("death-text");

	//String node contained for each Setting type.
	private final String node;

	private Setting(String node) {
		this.node = node;
	}

	public String node() {
		return this.node;
	}

	public String toString()
	{
		return this.node;
	}
}
