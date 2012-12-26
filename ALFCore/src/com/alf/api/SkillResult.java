package com.alf.api;

/**
 * Describes the result of a Skill's invocation.
 * @author Eteocles
 */
public class SkillResult {
	
	public final Object[] args;
	public final ResultType type;
	public final boolean showMessage;
	
	public static final SkillResult FAIL = new SkillResult(ResultType.FAIL, false, new Object[0]);
	public static final SkillResult INVALID_TARGET = new SkillResult(ResultType.INVALID_TARGET, true, new Object[0]);
	public static final SkillResult LOW_MANA = new SkillResult(ResultType.LOW_MANA, true, new Object[0]);
	public static final SkillResult LOW_HEALTH = new SkillResult(ResultType.LOW_HEALTH, true, new Object[0]);
	public static final SkillResult NORMAL = new SkillResult(ResultType.NORMAL, false, new Object[0]);
	public static final SkillResult SKIP_POST_USAGE = new SkillResult(ResultType.SKIP_POST_USAGE, false, new Object[0]);
	public static final SkillResult START_DELAY = new SkillResult(ResultType.START_DELAY, false, new Object[0]);
	public static final SkillResult CANCELLED = new SkillResult(ResultType.CANCELLED, false, new Object[0]);
	public static final SkillResult REMOVED_EFFECT = new SkillResult(ResultType.REMOVED_EFFECT, false, new Object[0]);
	public static final SkillResult INVALID_TARGET_NO_MSG = new SkillResult(ResultType.INVALID_TARGET, false, new Object[0]);
	public static final SkillResult LOW_STAMINA = new SkillResult(ResultType.LOW_STAMINA, true, new Object[0]);
	public static final SkillResult NO_COMBAT = new SkillResult(ResultType.NO_COMBAT, true, new Object[0]);

	/**
	 * Construct the Skill Result.
	 * @param type
	 * @param showMessage
	 * @param args
	 */
	public SkillResult(ResultType type, boolean showMessage, Object[] args) {
		this.type = type;
		this.args = args;
		this.showMessage = showMessage;
	}
	
	/**
	 * Describes a type of result.
	 * @author Eteocles
	 */
	public static enum ResultType {
		CANCELLED,
		INVALID_TARGET,
		FAIL,
		LOW_MANA,
		LOW_HEALTH,
		LOW_LEVEL,
		LOW_STAMINA,
		MISSING_REAGENT,
		NO_COMBAT,
		NORMAL,
		ON_GLOBAL_COOLDOWN,
		ON_COOLDOWN,
		REMOVED_EFFECT,
		SKIP_POST_USAGE,
		START_DELAY;
	}
}
