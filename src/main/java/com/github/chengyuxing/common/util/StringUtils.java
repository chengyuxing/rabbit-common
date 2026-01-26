package com.github.chengyuxing.common.util;


import com.github.chengyuxing.common.StringFormatter;
import com.github.chengyuxing.common.tuple.Pair;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String util.
 */
public final class StringUtils {
    /**
     * e.g. {@code 1, -1, 3.14}
     */
    public static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?(0|[1-9]\\d*)(\\.\\d+)?");
    public static final String NEW_LINE = "\n";
    /**
     * String formatter.
     */
    public static final StringFormatter FMT = new StringFormatter();

    /**
     * Split content by regex and save the splitters,
     * e.g. {@code aaa##bbb%%ddd}<br>
     * RegexResult
     * <blockquote>
     * {@code (?<symbol>##|%%)} symbol :   { [ aaa, bbb, ccc] , [ ## , %% ] } <br>
     *
     * </blockquote>
     *
     * @param s         string
     * @param regex     regex
     * @param groupName regex splitter group name
     * @return [each parts, splitters]
     */
    public static @NotNull Pair<List<String>, List<String>> regexSplit(@NotNull String s, @Language("Regexp") @NotNull String regex, @NotNull String groupName) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        int splitIndex = 0;
        List<String> items = new ArrayList<>();
        List<String> splitSymbols = new ArrayList<>();
        while (m.find()) {
            items.add(s.substring(splitIndex, m.start(groupName)));
            splitSymbols.add(m.group(groupName));
            splitIndex = m.end(groupName);
        }
        items.add(s.substring(splitIndex));
        return Pair.of(items, splitSymbols);
    }

    /**
     * Replace all and returns the founded terms.
     * <p>e.g. <code>Hello world!</code></p>
     * <blockquote>
     * <pre>replaceAll("Hello world!", "ll|o", "#$0#")</pre>
     * </blockquote>
     * <blockquote>
     * <pre>(He#ll##o# w#o#rld!, [ll, o, o])</pre>
     * </blockquote>
     *
     * @param s           string
     * @param regex       regex
     * @param replacement replacement
     * @return [new string, founded terms]
     */
    public static @NotNull Pair<String, List<String>> replaceAll(@NotNull String s, @Language("Regexp") @NotNull String regex, @NotNull String replacement) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        List<String> found = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            found.add(m.group());
            String resolvedReplacement = replacement;
            for (int i = 0, j = m.groupCount(); i <= j; i++) {
                resolvedReplacement = resolvedReplacement.replace("$" + i, Matcher.quoteReplacement(m.group(i)));
            }
            m.appendReplacement(sb, resolvedReplacement);
        }
        m.appendTail(sb);
        return Pair.of(sb.toString(), found);
    }

    /**
     * Checks if the given string starts with any of the specified keywords.
     *
     * @param str      The string to check.
     * @param keywords The keywords to look for at the beginning of the string.
     * @return true if the string starts with any of the keywords, false otherwise.
     */
    public static boolean startsWiths(@NotNull String str, String @NotNull ... keywords) {
        for (String keyword : keywords) {
            if (str.startsWith(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given string ends with any of the specified keywords.
     *
     * @param str      The string to check.
     * @param keywords The keywords to look for at the end of the string.
     * @return true if the string ends with any of the keywords, false otherwise.
     */
    public static boolean endsWiths(@NotNull String str, String @NotNull ... keywords) {
        for (String keyword : keywords) {
            if (str.endsWith(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given string starts with the specified prefix, ignoring case.
     *
     * @param str    the string to check
     * @param prefix the prefix to look for
     * @return true if the string starts with the prefix, ignoring case, false otherwise
     */
    public static boolean startsWithIgnoreCase(@NotNull String str, @NotNull String prefix) {
        int len = prefix.length();
        if (len == 0) {
            return true;
        }
        if (len > str.length()) {
            return false;
        }
        return str.regionMatches(true, 0, prefix, 0, len);
    }

    /**
     * Determines if the given string ends with the specified suffix, ignoring case.
     *
     * @param str    The string to check.
     * @param suffix The suffix to look for at the end of the string.
     * @return true if the string ends with the specified suffix, ignoring case, false otherwise.
     */
    public static boolean endsWithIgnoreCase(@NotNull String str, @NotNull String suffix) {
        int len = suffix.length();
        if (len == 0) {
            return true;
        }
        if (len > str.length()) {
            return false;
        }
        return str.regionMatches(true, str.length() - len, suffix, 0, len);
    }

    /**
     * Checks if the given string starts with any of the specified keywords, ignoring case.
     *
     * @param str      The string to check.
     * @param keywords The keywords to look for at the beginning of the string.
     * @return true if the string starts with any of the keywords, ignoring case, false otherwise.
     */
    public static boolean startsWithsIgnoreCase(@NotNull String str, String @NotNull ... keywords) {
        for (String keyword : keywords) {
            if (startsWithIgnoreCase(str, keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given string ends with any of the specified keywords, ignoring case.
     *
     * @param str      The string to check.
     * @param keywords The keywords to look for at the end of the string.
     * @return true if the string ends with any of the keywords, ignoring case, false otherwise.
     */
    public static boolean endsWithsIgnoreCase(@NotNull String str, String @NotNull ... keywords) {
        for (String keyword : keywords) {
            if (endsWithIgnoreCase(str, keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches for the first occurrence of the specified target string, ignoring case,
     * within the source string starting from a specified index.
     *
     * @param source    The string to be searched.
     * @param target    The string to search for.
     * @param fromIndex The index from which to start the search.
     * @return The index of the first occurrence of the target string, or -1 if not found.
     */
    public static int indexOfIgnoreCase(@NotNull String source, @NotNull String target, int fromIndex) {
        int srcLen = source.length();
        int tgtLen = target.length();

        if (fromIndex < 0) fromIndex = 0;
        if (tgtLen == 0) return fromIndex;
        if (tgtLen > srcLen) return -1;

        int max = srcLen - tgtLen;
        for (int i = fromIndex; i <= max; i++) {
            if (source.regionMatches(true, i, target, 0, tgtLen)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches for the first occurrence of the specified target string within the source string, ignoring case.
     *
     * @param source the string to be searched
     * @param target the string to search for
     * @return the index of the first occurrence of the target string in the source string, or -1 if the target is not found
     */
    public static int indexOfIgnoreCase(String source, String target) {
        return indexOfIgnoreCase(source, target, 0);
    }

    /**
     * Finds the index of the first non-whitespace character in the source string where the target string starts.
     * If the target string is not found starting from any non-whitespace character, returns -1.
     *
     * @param source The source string to search within.
     * @param target The target string to find in the source.
     * @return The index of the first non-whitespace character in the source where the target starts, or -1 if not found.
     */
    public static int indexOfNonWhitespace(@NotNull String source, @NotNull String target) {
        int len = source.length();
        int i = 0;

        while (i < len) {
            char c = source.charAt(i);
            if (c != ' ' && c != '\t' && !Character.isWhitespace(c)) {
                break;
            }
            i++;
        }
        if (i + target.length() > len) {
            return -1;
        }
        return source.startsWith(target, i) ? i : -1;
    }

    /**
     * Searches for the last occurrence of a non-whitespace substring within a given string.
     * This method first finds the last non-whitespace character in the source string and then
     * checks if the target substring matches at that position or before it. If a match is found,
     * the starting index of the match is returned; otherwise, -1 is returned.
     *
     * @param source The string to search within. Must not be null.
     * @param target The substring to search for. Must not be null.
     * @return The starting index of the last occurrence of the target substring within the source,
     * ignoring any trailing whitespace in the source. Returns -1 if the target is not found.
     */
    public static int lastIndexOfNonWhitespace(@NotNull String source, @NotNull String target) {
        int len = source.length();
        int tgtLen = target.length();

        int i = len - 1;
        while (i >= 0) {
            char c = source.charAt(i);
            if (c != ' ' && c != '\t' && !Character.isWhitespace(c)) {
                break;
            }
            i--;
        }

        int start = i - tgtLen + 1;
        if (start < 0) {
            return -1;
        }
        return source.regionMatches(start, target, 0, tgtLen) ? start : -1;
    }

    /**
     * Checks if the target string is contained within the source string, ignoring case.
     *
     * @param source The string to search within.
     * @param target The string to search for.
     * @return true if the target string is found within the source string, ignoring case; false otherwise.
     */
    public static boolean containsIgnoreCase(String source, String target) {
        return indexOfIgnoreCase(source, target) > -1;
    }

    /**
     * Determines if the source string contains any of the target strings, ignoring case.
     *
     * @param source  The string to search within.
     * @param targets The strings to search for within the source string.
     * @return true if any of the target strings are found within the source string, ignoring case; false otherwise.
     */
    public static boolean containsAnyIgnoreCase(@NotNull String source, String @NotNull ... targets) {
        for (String target : targets) {
            if (containsIgnoreCase(source, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the source string contains any of the target strings.
     *
     * @param source  The string to search within.
     * @param targets The strings to search for within the source string.
     * @return true if any of the target strings are found within the source string; false otherwise.
     */
    public static boolean containsAny(@NotNull String source, String @NotNull ... targets) {
        for (String target : targets) {
            if (source.contains(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the source string is equal to any of the target strings, ignoring case.
     *
     * @param source the string to compare against the targets
     *               * @param targets an array of strings to be compared with the source
     * @return true if the source string matches any of the target strings, ignoring case; false otherwise
     */
    public static boolean equalsAnyIgnoreCase(@NotNull String source, String @NotNull ... targets) {
        for (String target : targets) {
            if (source.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the source string is equal to any of the target strings provided.
     *
     * @param source  the string to be compared against the targets
     * @param targets variable number of strings to compare with the source
     * @return true if the source string matches any of the target strings, false otherwise
     */
    public static boolean equalsAny(@NotNull String source, String @NotNull ... targets) {
        for (String target : targets) {
            if (source.equals(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the source string contains all the target strings, ignoring case.
     *
     * @param source  the string to search in
     * @param targets variable number of strings to find within the source
     * @return true if the source contains all the target strings, ignoring case, false otherwise
     */
    public static boolean containsAllIgnoreCase(@NotNull String source, String @NotNull ... targets) {
        for (String target : targets) {
            if (!containsIgnoreCase(source, target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the source string contains all of the target strings.
     *
     * @param source the string to be searched
     *               * @param targets variable number of strings that are expected to be found within the source string
     * @return true if the source string contains all of the target strings, false otherwise
     */
    public static boolean containsAll(@NotNull String source, String @NotNull ... targets) {
        for (String target : targets) {
            if (!source.contains(target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the provided string is empty or null.
     *
     * @param str the string to check
     * @return true if the string is null, blank, or only contains whitespace; false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || isBlank(str);
    }

    /**
     * Checks if the provided string is blank, meaning it contains only whitespace characters or is empty.
     * Whitespace characters include spaces, tabs, and line breaks.
     *
     * @param s the string to check, must not be null
     * @return true if the string is blank (contains only whitespace or is empty), false otherwise
     */
    public static boolean isBlank(@NotNull String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts the number of non-overlapping occurrences of a substring in a given string, ignoring case.
     *
     * @param str the string to search within. If null, the method returns 0.
     * @param sub the substring to search for. If null or an empty string, the method returns 0.
     * @return the number of times the substring occurs in the string, ignoring case.
     */
    public static int countOccurrencesIgnoreCase(@NotNull String str, @NotNull String sub) {
        if (sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        int fromIndex = 0;
        while ((fromIndex = indexOfIgnoreCase(str, sub, fromIndex)) != -1) {
            count++;
            fromIndex += sub.length();
        }
        return count;
    }

    /**
     * Counts the number of non-overlapping occurrences of a substring within a given string.
     *
     * @param str The string to search within. If null, the method returns 0.
     * @param sub The substring to search for. If null or an empty string, the method returns 0.
     * @return The number of non-overlapping occurrences of the substring in the given string.
     */
    public static int countOccurrences(@NotNull String str, @NotNull String sub) {
        if (sub.isEmpty()) {
            return 0;
        }
        int count = 0;
        if (sub.length() == 1) {
            char c = sub.charAt(0);
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == c) {
                    count++;
                }
            }
            return count;
        }

        int fromIndex = 0;
        while ((fromIndex = str.indexOf(sub, fromIndex)) != -1) {
            count++;
            fromIndex += sub.length();
        }
        return count;
    }

    /**
     * Check if provided object string literal is number or not.
     *
     * @param o string literal
     * @return true or false
     */
    public static boolean isNumber(Object o) {
        if (o == null) {
            return false;
        }
        return NUMBER_PATTERN.matcher(o.toString()).matches();
    }

    /**
     * Check if the provided str is integer which {@code >= 0} .
     *
     * @param str the string to check
     * @return true if str {@code >= 0} ,false otherwise
     */
    public static boolean isNonNegativeInteger(String str) {
        if (str.equals("0")) {
            return true;
        }
        return isAsciiDigits(str);
    }

    /**
     * Checks if the provided str includes 0 - 9 only.
     *
     * @param str the string to check
     * @return true if the str is a non-empty string and all characters are 0 - 9, false otherwise
     */
    public static boolean isAsciiDigits(@NotNull String str) {
        int len = str.length();
        if (len == 0) return false;
        for (int i = 0; i < len; i++) {
            if (!isAsciiDigit(str.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Check the char is 0 - 9 or not.
     *
     * @param c the char to check
     * @return if 0 -9 true, false otherwise
     */
    public static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Removes empty lines from the provided string content.
     *
     * @param content the string from which empty lines will be removed
     * @return a new string with all empty lines removed
     */
    public static String removeEmptyLine(@NotNull String content) {
        return content.replaceAll("\\s*\r?\n", NEW_LINE);
    }

    /**
     * Repeats the given string a specified number of times.
     * If the number of times is less than or equal to 0, an empty string is returned.
     *
     * @param str   the string to be repeated
     * @param times the number of times to repeat the string
     * @return a new string consisting of the original string repeated multiple times
     */
    public static String repeat(@NotNull String str, int times) {
        if (times <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(str);
        }
        return builder.toString();
    }

    /**
     * Encrypts or decrypts the given content using XOR operation with the provided key.
     *
     * @param content the string to be encrypted or decrypted
     * @param key     the key used for encryption or decryption, which should be a non-empty string
     * @return the encrypted or decrypted string based on the input content and key
     */
    public static String xorEncryptDecrypt(@NotNull String content, @NotNull String key) {
        char[] keys = key.toCharArray();
        char[] contentChars = content.toCharArray();
        char[] result = new char[content.length()];
        for (int i = 0; i < contentChars.length; i++) {
            result[i] = (char) (contentChars[i] ^ keys[i % keys.length]);
        }
        return new String(result);
    }

    /**
     * Computes the hash of the given content using the specified algorithm.
     *
     * @param content   the string to be hashed
     * @param algorithm the name of the hashing algorithm (e.g., "MD5", "SHA-256")
     * @return a string representing the hexadecimal value of the hash
     */
    public static @NotNull String hash(@NotNull String content, String algorithm) {
        return hash(content.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    /**
     * Computes the hash of the given content using the specified algorithm.
     *
     * @param content   the byte array to be hashed
     * @param algorithm the name of the hashing algorithm (e.g., "MD5", "SHA-256")
     * @return a string representing the hexadecimal value of the hash
     */
    public static @NotNull String hash(byte[] content, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] digestBytes = digest.digest(content);
            StringBuilder hexString = new StringBuilder(2 * digestBytes.length);
            for (byte b : digestBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
