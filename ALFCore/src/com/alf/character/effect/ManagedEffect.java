package com.alf.character.effect;

import com.alf.character.CharacterTemplate;

/**
 * Encapsulates an Effect and Character type.
 * @author Eteocles
 */
public class ManagedEffect {
	public final Effect effect;
	public final CharacterTemplate character;

	/**
	 * Constructs a Managed Effect.
	 * @param character
	 * @param effect
	 */
	protected ManagedEffect(CharacterTemplate character, Effect effect) {
		this.effect = effect;
		this.character = character;
	}

	/**
	 * Get the encapsulated effect.
	 * @return
	 */
	public Effect getEffect() {
		return this.effect;
	}

	public int hashCode() {
		int prime = 37;
		int result = 7;
		result = prime * result + this.effect.hashCode();
		result = prime * result + this.character.hashCode();
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ManagedEffect other = (ManagedEffect)obj;
		if (!this.effect.equals(other.effect))
			return false;
		if (!this.character.equals(other.character))
			return false;
		return true;
	}
}
