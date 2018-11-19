/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.loader.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

/**
 * Helper class for resolving placeholders in texts. Usually applied to file paths.
 * 
 * <p>
 * A text may contain {@code $ ...} placeholders, to be resolved as system properties:
 * e.g. {@code $ user.dir} . Default values can be supplied using the ":" separator
 * between key and value.
 * 
 * <p>
 * 
 * Adapted from Spring.
 * 
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * 
 * @see System#getProperty(String)
 */
public abstract class SystemPropertyUtils {

	/** Prefix for system property placeholders: "${" */
	public static final String PLACEHOLDER_PREFIX = "${";

	/** Suffix for system property placeholders: "}" */
	public static final String PLACEHOLDER_SUFFIX = "}";

	/** Value separator for system property placeholders: ":" */
	public static final String VALUE_SEPARATOR = ":";

	private static final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper();

	/**
	 * Resolve ${...} placeholders in the given text, replacing them with corresponding
	 * system property values.
	 * @param text the String to resolve
	 * @return the resolved String
	 * @see #PLACEHOLDER_PREFIX
	 * @see #PLACEHOLDER_SUFFIX
	 * @throws IllegalArgumentException if there is an unresolvable placeholder
	 */
	public static String resolvePlaceholders(String text) {
		return helper.replacePlaceholders(text);
	}

	static protected class PropertyPlaceholderHelper {

		private static final String simplePrefix = PLACEHOLDER_PREFIX.substring(1);

		/**
		 * Replaces all placeholders of format {@code $ name} with the value returned from
		 * the supplied {@link PlaceholderResolver}.
		 * @param value the value containing the placeholders to be replaced.
		 * @return the supplied value with placeholders replaced inline.
		 */
		public String replacePlaceholders(String value) {
			Assert.notNull(value, "Argument 'value' must not be null.");
			return parseStringValue(value, value, new HashSet<String>());
		}

		private String parseStringValue(String value, String current,
				Set<String> visitedPlaceholders) {

			StringBuilder buf = new StringBuilder(current);

			int startIndex = current.indexOf(PLACEHOLDER_PREFIX);
			while (startIndex != -1) {
				int endIndex = findPlaceholderEndIndex(buf, startIndex);
				if (endIndex != -1) {
					String placeholder = buf.substring(
							startIndex + PLACEHOLDER_PREFIX.length(), endIndex);
					String originalPlaceholder = placeholder;
					if (!visitedPlaceholders.add(originalPlaceholder)) {
						throw new IllegalArgumentException(
								"Circular placeholder reference '" + originalPlaceholder
										+ "' in property definitions");
					}
					// Recursive invocation, parsing placeholders contained in the
					// placeholder
					// key.
					placeholder = parseStringValue(value, placeholder,
							visitedPlaceholders);
					// Now obtain the value for the fully resolved key...
					String propVal = resolvePlaceholder(value, placeholder);
					if (propVal == null && VALUE_SEPARATOR != null) {
						int separatorIndex = placeholder.indexOf(VALUE_SEPARATOR);
						if (separatorIndex != -1) {
							String actualPlaceholder = placeholder.substring(0,
									separatorIndex);
							String defaultValue = placeholder.substring(separatorIndex
									+ VALUE_SEPARATOR.length());
							propVal = resolvePlaceholder(value, actualPlaceholder);
							if (propVal == null) {
								propVal = defaultValue;
							}
						}
					}
					if (propVal != null) {
						// Recursive invocation, parsing placeholders contained in the
						// previously resolved placeholder value.
						propVal = parseStringValue(value, propVal, visitedPlaceholders);
						buf.replace(startIndex, endIndex + PLACEHOLDER_SUFFIX.length(),
								propVal);
						startIndex = buf.indexOf(PLACEHOLDER_PREFIX,
								startIndex + propVal.length());
					}
					else {
						// Proceed with unprocessed value.
						startIndex = buf.indexOf(PLACEHOLDER_PREFIX, endIndex
								+ PLACEHOLDER_SUFFIX.length());
					}
					visitedPlaceholders.remove(originalPlaceholder);
				}
				else {
					startIndex = -1;
				}
			}

			return buf.toString();
		}

		private String resolvePlaceholder(String text, String placeholderName) {
			try {
				String propVal = System.getProperty(placeholderName);
				if (propVal == null) {
					// Fall back to searching the system environment.
					propVal = System.getenv(placeholderName);
				}
				return propVal;
			}
			catch (Throwable ex) {
				System.err.println("Could not resolve placeholder '" + placeholderName
						+ "' in [" + text + "] as system property: " + ex);
				return null;
			}
		}

		private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
			int index = startIndex + PLACEHOLDER_PREFIX.length();
			int withinNestedPlaceholder = 0;
			while (index < buf.length()) {
				if (substringMatch(buf, index, PLACEHOLDER_SUFFIX)) {
					if (withinNestedPlaceholder > 0) {
						withinNestedPlaceholder--;
						index = index + PLACEHOLDER_SUFFIX.length();
					}
					else {
						return index;
					}
				}
				else if (substringMatch(buf, index,
						PropertyPlaceholderHelper.simplePrefix)) {
					withinNestedPlaceholder++;
					index = index + PropertyPlaceholderHelper.simplePrefix.length();
				}
				else {
					index++;
				}
			}
			return -1;
		}

		private static boolean substringMatch(CharSequence str, int index,
				CharSequence substring) {
			for (int j = 0; j < substring.length(); j++) {
				int i = index + j;
				if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
					return false;
				}
			}
			return true;
		}

		private static class Assert {

			public static void notNull(Object target, String message) {
				if (target == null) {
					throw new IllegalStateException(message);
				}
			}

		}

	}

}
