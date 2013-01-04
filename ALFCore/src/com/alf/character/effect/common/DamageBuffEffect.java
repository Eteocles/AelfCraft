package com.alf.character.effect.common;

import org.bukkit.entity.Player;

import com.alf.character.Alf;
import com.alf.character.CharacterDamageManager;
import com.alf.character.effect.EffectType;
import com.alf.character.effect.ExpirableEffect;
import com.alf.skill.Skill;

public class DamageBuffEffect extends ExpirableEffect {
	private final String applyText;
	private final String expireText;
	
	private final double amplifier;
	private final CharacterDamageManager damageManager;
	
	public DamageBuffEffect(Skill skill, String name, long duration,
			double amplifier, String applyText, String expireText, CharacterDamageManager dm) {
		super(skill, name, duration);
		this.types.add(EffectType.DISPELLABLE);
		this.types.add(EffectType.BENEFICIAL);
		this.types.add(EffectType.MAGIC);
		this.types.add(EffectType.PHYSICAL);
		this.types.add(EffectType.PHYS_BUFF);
		
		this.applyText = applyText;
		this.expireText = expireText;
		this.amplifier = amplifier;
		this.damageManager = dm;
	}
	
	/**
	 * Get the amplifier.
	 * @return
	 */
	public double getAmplifier() {
		return this.amplifier;
	}
	
	/**
	 * Apply to the given alf.
	 */
	public void applyToAlf(Alf alf) {
		super.applyToAlf(alf);
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), this.applyText, new Object[] { player.getDisplayName() });
		damageManager.addAlfDamageBuff(player, this.amplifier);
	}
	
	/**
	 * Remove from the given alf.
	 */
	public void removeFromAlf(Alf alf) {
		super.removeFromAlf(alf);
		Player player = alf.getPlayer();
		broadcast(player.getLocation(), this.expireText, new Object[] { player.getDisplayName() });
		damageManager.removeAlfDamageBuff(player);
	}
}
