package com.alf.character.effect;

import java.util.*;


import net.minecraft.server.v1_4_6.EntityLiving;
import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.MobEffect;
import net.minecraft.server.v1_4_6.Packet41MobEffect;
import net.minecraft.server.v1_4_6.Packet42RemoveMobEffect;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.alf.AlfCore;
import com.alf.character.Alf;
import com.alf.character.CharacterTemplate;
import com.alf.character.Monster;
import com.alf.skill.Skill;

/**
 * The Effect class describes a specific condition applied to an Alf.
 * @author Eteocles
 */
public class Effect {

	//Name of this effect.
	protected final String name;
	//Skill bound to this effect.
	protected final Skill skill;
	//Main plugin reference.
	protected final AlfCore plugin;
	//Types of effects that fit this Effect.
	public final Set<EffectType> types = EnumSet.noneOf(EffectType.class);
	//Length of apply time.
	protected long applyTime;
	//Whether the effect is persistent or not.
	private boolean persistent = false;
	//Set of the potion effects encapsulated by this Effect.
	private Set<PotionEffect> potionEffects = new HashSet<PotionEffect>();
	//Set of the mob effects encapsulated by this Effect.
	private Set<MobEffect> mobEffects = new HashSet<MobEffect>();

	public Effect(Skill skill, String name) {
		this(skill == null ? null : skill.plugin, skill, name, new EffectType[0]);
	}

	public Effect(Skill skill, String name, EffectType[] types) {
		this(skill.plugin, skill, name, types);
	}

	/**
	 * Construct an Effect.
	 * @param plugin
	 * @param skill
	 * @param name
	 * @param types
	 */
	public Effect(AlfCore plugin, Skill skill, String name, EffectType[] types) {
		this.name = name;
		this.skill = skill;
		this.plugin = plugin;
		if (types != null)
			for (EffectType type : types)
				this.types.add(type);
	}

	/**
	 * Apply this effect to a given character.
	 * @param character
	 */
	public void apply(CharacterTemplate character) {
		if (character instanceof Alf)
			applyToAlf((Alf) character);
		else if (character instanceof Monster)
			applyToMonster((Monster) character);
		else {
			this.applyTime = System.currentTimeMillis();
		}
	}

	/**
	 * Apply this effect to a given monster.
	 * @param monster
	 */
	public void applyToMonster(Monster monster) {
		this.applyTime = System.currentTimeMillis();
		LivingEntity le;
		if (! this.potionEffects.isEmpty()) {
			le = monster.getEntity();
			for (PotionEffect pEffect : this.potionEffects)
				le.addPotionEffect(pEffect);
		}
	}

	/**
	 * Apply this effect to a given Alf.
	 * @param alf
	 */
	public void applyToAlf(Alf alf) {
		this.applyTime = System.currentTimeMillis();
		EntityPlayer ePlayer;
		if (! this.potionEffects.isEmpty()) {
			ePlayer = (((CraftPlayer)alf.getPlayer()).getHandle());
			Player p = alf.getPlayer();
			for (PotionEffect pEffect : this.potionEffects)
				p.addPotionEffect(pEffect);
			for (MobEffect mEffect : this.mobEffects)
				ePlayer.playerConnection.sendPacket(new Packet41MobEffect(ePlayer.id, mEffect));
		}
	}

	/**
	 * Reapply this effect to the Alf.
	 * @param alf
	 */
	public void reapplyToAlf(Alf alf) {
		EntityPlayer ePlayer = ((CraftPlayer)alf.getPlayer()).getHandle();
		Player p = alf.getPlayer();
		for (PotionEffect pEffect : this.potionEffects)
			p.addPotionEffect(pEffect);
		for (MobEffect mEffect : this.mobEffects)
			ePlayer.playerConnection.sendPacket(new Packet41MobEffect(ePlayer.id, mEffect));
	}

	/**
	 * Remove this effect from the given Character.
	 * @param character
	 */
	public void remove(CharacterTemplate character) {
		if (character instanceof Alf)
			removeFromAlf((Alf) character);
		else if (character instanceof Monster)
			removeFromMonster((Monster) character);
	}

	/**
	 * Remove this effect from the Alf.
	 * @param alf
	 */
	public void removeFromAlf(Alf alf) {
		EntityPlayer ePlayer = ((CraftPlayer) alf.getPlayer()).getHandle();
		Player p;
		if (! this.potionEffects.isEmpty()) {
			p = alf.getPlayer();
			for (PotionEffect pEffect : this.potionEffects) {
				p.removePotionEffect(pEffect.getType());
				ePlayer.playerConnection.sendPacket(
						new Packet42RemoveMobEffect(ePlayer.id, 
								new MobEffect(pEffect.getType().getId(), pEffect.getDuration())));
			}
		}
		if (! this.mobEffects.isEmpty())
			for (MobEffect mEffect : this.mobEffects)
				ePlayer.playerConnection.sendPacket(new Packet42RemoveMobEffect(ePlayer.id, mEffect));
	}

	/**
	 * Remove this effect from the Monster.
	 * @param monster
	 */
	public void removeFromMonster(Monster monster) {
		EntityLiving eLiving;
		if (! this.potionEffects.isEmpty()) {
			eLiving = ((CraftLivingEntity)monster.getEntity()).getHandle();
			for (PotionEffect pEffect : this.potionEffects)
				eLiving.getEffects().remove(pEffect);
		}
	}

	/**
	 * Broadcast the effect's casting.
	 * @param source
	 * @param message
	 * @param args
	 */
	public void broadcast(Location source, String message, Object[] args) {
		this.skill.broadcast(source, message, args);
	}

	/**
	 * Effect equality.
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		Effect other = (Effect) obj;
		if (this.name == null)
			if (other.name != null)
				return false;
			else if (! this.name.equals(other.name))
				return false;
		return true;
	}
	
	/**
	 * Get Effect name.
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Get bound Skill name.
	 * @return
	 */
	public Skill getSkill() {
		return this.skill;
	}
	
	/**
	 * Hash code for the Effect.
	 */
	public int hashCode() {
		return this.name.hashCode();
	}
	
	/**
	 * Get the duration of time since the Effect's application.
	 * @return
	 */
	public long getApplyTime() {
		return this.applyTime;
	}
	
	/**
	 * Whether this Effect is persistent.
	 * @return
	 */
	public boolean isPersistent() {
		return this.persistent;
	}
	
	/**
	 * Whether this Effect is of a certain type.
	 * @param type
	 * @return
	 */
	public boolean isType(EffectType type) {
		return this.types.contains(type);
	}
	
	/**
	 * Set whether this Effect is persistent.
	 * @param persistent
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
	
	/**
	 * Add a Mob Effect with the given parameters.
	 * @param id
	 * @param duration
	 * @param strength
	 * @param faked
	 */
	public void addMobEffect(int id, int duration, int strength, boolean faked) {
		addPotionEffect(id, duration, strength, faked);
	}
	
	/**
	 * Get the set of Mob Effects.
	 * @return
	 */
	public Set<MobEffect> getMobEffects() {
		return Collections.unmodifiableSet(this.mobEffects);
	}
	
	/**
	 * Get the set of Potion Effects.
	 * @return
	 */
	public Set<PotionEffect> getPotionEffects() {
		return Collections.unmodifiableSet(this.potionEffects);
	}
	
	/**
	 * Add a potion effect.
	 * @param id
	 * @param duration
	 * @param strength
	 * @param faked
	 */
	public void addPotionEffect(int id, int duration, int strength, boolean faked) {
		if (!faked)
			this.potionEffects.add(new PotionEffect(PotionEffectType.getById(id), duration, strength));
	}
	
	/**
	 * Add a potion effect.
	 * @param pEffect
	 * @param faked - whether to actually add a potion effect or just to show its packet
	 */
	public void addPotionEffect(PotionEffect pEffect, boolean faked) {
		if (! faked)
			this.potionEffects.add(pEffect);
		else
			this.mobEffects.add(
					new MobEffect(pEffect.getType().getId(), pEffect.getDuration(), pEffect.getAmplifier())
			);
	}
}
