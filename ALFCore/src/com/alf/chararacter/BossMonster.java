package com.alf.chararacter;

import org.bukkit.entity.LivingEntity;

import com.alf.AlfCore;

/**
 * Encapsulates a monster with upgraded specs.
 * @author Eteocles
 * TODO Add region support so it can't leave a certain area.
 */
public class BossMonster extends Monster {

	public BossMonster(AlfCore plugin, LivingEntity lEntity, String name) {
		super(plugin, lEntity, name);
	}

}
