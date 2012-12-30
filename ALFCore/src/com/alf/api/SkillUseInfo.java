package com.alf.api;

import com.alf.character.CharacterTemplate;
import com.alf.skill.Skill;

/**
 * Encapsulates a Skill and the Character using it.
 * @author Eteocles
 */
public class SkillUseInfo {
	
	private final CharacterTemplate character;
	private final Skill skill;
	
	/**
	 * Construct the SkillUseInfo.
	 * @param character
	 * @param skill
	 */
	public SkillUseInfo(CharacterTemplate character, Skill skill) {
		this.character = character;
		this.skill = skill;
	}
	
	/**
	 * Get the Character using the Skill.
	 * @return
	 */
	public CharacterTemplate getCharacter()
	{	return this.character;	}
	
	/**
	 * Get the skill used.
	 * @return
	 */
	public Skill getSkill()
	{	return this.skill;	}
}
