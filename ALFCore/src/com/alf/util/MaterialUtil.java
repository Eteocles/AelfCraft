package com.alf.util;

import org.bukkit.Material;

/**
 * Contains utility methods for Materials.
 * @author Eteocles
 */
public class MaterialUtil {

	/** Capitalize all alphabetical components of the input string. */
	public static String capitalize(String input) {
		char[] chars = input.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i]))
				found = false;
		}
		return String.valueOf(chars);
	}

	/** Return program friendly name of type. */
	public static Object getFriendlyName(Material type) {
		return getFriendlyName(type.toString());
	}
	
	/** Return program friendly name of input. */
	public static String getFriendlyName(String input) {
		return capitalize(input.toLowerCase().replaceAll("_", " "));
	}
	
}
