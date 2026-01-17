package com.github.chengyuxing.common.util;

import org.jetbrains.annotations.NotNull;

public class NamingUtils {
    /**
     * Converts a kebab-case string to camelCase.
     * The first word is in lowercase, and each subsequent word starts with an uppercase letter.
     *
     * @param str the kebab-case string to be converted
     * @return the converted camelCase string
     */
    public static @NotNull String kebabToCamel(@NotNull String str) {
        return delimiterToCamel(str, '-');
    }

    /**
     * Converts a snake_case string to camelCase.
     * The first word is in lowercase, and each subsequent word starts with an uppercase letter.
     *
     * @param str the snake_case string to be converted
     * @return the converted camelCase string
     */
    public static @NotNull String snakeToCamel(@NotNull String str) {
        return delimiterToCamel(str, '_');
    }

    /**
     * Converts a camelCase string to snake_case.
     *
     * @param str the camelCase string to be converted
     * @return the converted snake_case string
     */
    public static @NotNull String camelToSnake(@NotNull String str) {
        return camelToDelimiter(str, '_');
    }

    /**
     * Converts a camelCase string to kebab-case.
     *
     * @param str the camelCase string to be converted
     * @return the converted kebab-case string
     */
    public static String camelToKebab(@NotNull String str) {
        return camelToDelimiter(str, '-');
    }

    /**
     * Converts a string with a specified delimiter to camelCase.
     * The first word is in lowercase, and each subsequent word starts with an uppercase letter.
     *
     * @param str       the string with delimiters to be converted
     * @param delimiter the character used as a delimiter in the input string
     * @return the converted camelCase string
     */
    private static @NotNull String delimiterToCamel(@NotNull String str, char delimiter) {
        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        boolean upperNext = false;

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == delimiter) {
                upperNext = true;
            } else if (upperNext) {
                sb.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Transforms a camelCase string into a string separated by a specified delimiter.
     * Each uppercase letter in the input string is preceded by the given delimiter and converted to lowercase,
     * except for the first character, which is only converted to lowercase if it is uppercase.
     *
     * @param str       the camelCase string to be transformed
     * @param delimiter the character used as a delimiter in the resulting string
     * @return the transformed string with delimiters
     */
    private static @NotNull String camelToDelimiter(@NotNull String str, char delimiter) {
        int len = str.length();
        StringBuilder sb = new StringBuilder(len + 4);

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append(delimiter);
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
