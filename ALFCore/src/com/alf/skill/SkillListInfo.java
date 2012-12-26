package com.alf.skill;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.alf.chararacter.classes.AlfClass;

/**
 * Encapsulates an AlfClass and a Skill.
 * @author Eteocles
 */
public class SkillListInfo {

	protected final AlfClass alfClass;
	protected final Skill skill;

	/**
	 * Construct the container.
	 * @param alfClass
	 * @param skill
	 */
	public SkillListInfo(AlfClass alfClass, Skill skill) {
		this.alfClass = alfClass;
		this.skill = skill;
	}

	/**
	 * Get the hash code.
	 */
	public int hashCode() {
		return 3 + this.alfClass.hashCode() * 17 + this.skill.hashCode();
	}

	/**
	 * Whether the container is equal to the parameter.
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (! (obj instanceof SkillListInfo))
			return false;

		SkillListInfo sli = (SkillListInfo) obj;
		return (sli.alfClass.equals(this.alfClass)) && (sli.skill.equals(this.skill));
	}

	/**
	 * Get the encapsulated Alf class.
	 * @return
	 */
	public AlfClass getAlfClass() {
		return this.alfClass;
	}

	/**
	 * Get the encapsulated skill.
	 * @return
	 */
	public Skill getSkill() {
		return this.skill;
	}

	/**
	 * Get a sorted set of a parameter map's SkillListInfo's entries.
	 * @param map
	 * @return
	 */
	public static SortedSet<Map.Entry<SkillListInfo, Integer>> entriesSortedByValues(Map<SkillListInfo, Integer> map) {
		SortedSet<Map.Entry<SkillListInfo, Integer>> sortedEntries = 
				new TreeSet<Map.Entry<SkillListInfo, Integer>> (new Comparator<Map.Entry<SkillListInfo, Integer>>() {
					/**
					 * Define the compare method for the comparator type.
					 * @param e1
					 * @param e2
					 * @return
					 */
					public int compare(Map.Entry<SkillListInfo, Integer> e1, Map.Entry<SkillListInfo, Integer> e2) {
						int res = ((Integer)e1.getValue()).compareTo((Integer)e2.getValue());
						if (res == 0)
							return ((SkillListInfo)e1.getKey()).skill.getName().compareTo(((SkillListInfo)e2.getKey()).skill.getName());
						return res;
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}
}
