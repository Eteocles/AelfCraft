package com.alf.chararacter.classes;

import org.bukkit.Material;

import com.alf.chararacter.CharacterDamageManager.ProjectileType;

public class AlfClass {

	public static enum ExperienceType
	{
		//Non-Profession Experience Types
		SKILL,
		KILLING,
		PVP,
		DEATH,
		ADMIN,
		EXTERNAL,
		QUESTING,
		//Profession Experience Types
		MINING, 
		ENCHANTING, SMITHING,
		LOGGING,
		FISHING,
		FARMING, SHEARING, BREEDING,
		ENGINEERING,
		BREWING, IMBUING,
		TRADING
	}

	public String getName() {
		throw new Error("Implement me!");
	}

	public boolean hasSkill(String name) {
		throw new Error("Implement me!");
	}

	public int getMaxLevel() {
		throw new Error("Implement me!");
	}

	public int getBaseMaxHealth() {
		// TODO Auto-generated method stub
		return 100;
	}

	public boolean isPrimary() {
		throw new Error("Implement me!");
	}

	public boolean isSecondary() {
		throw new Error("Implement me!");
	}

	public int getProjectileDamage(ProjectileType type) {
		throw new Error("Implement me!");
	}

	public double getProjDamageLevel(ProjectileType type) {
		throw new Error("Implement me!");
	}

	public int getItemDamage(Material item) {
		throw new Error("Implement me!");
	}

	public int getItemDamageLevel(Material item) {
		throw new Error("Implement me!");
	}
	
}
