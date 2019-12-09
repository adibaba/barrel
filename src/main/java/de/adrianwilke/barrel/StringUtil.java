package de.adrianwilke.barrel;

/**
 * String utilities.
 *
 * @author Adrian Wilke
 */
public abstract class StringUtil {

	public static String removePrefix(String string, String prefix) {
		if (string.startsWith(prefix)) {
			return string.substring(prefix.length());
		} else {
			throw new RuntimeException("Not a prefix: " + prefix + " | " + string);
		}

	}
}
