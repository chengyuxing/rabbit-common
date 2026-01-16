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
    //language=RegExp
    /**
     * e.g. {@code 1, -1, 3.14}
     */
    public static final String NUMBER_REGEX = "-?(0|[1-9]\\d*)(\\.\\d+)?";
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
    public static Pair<List<String>, List<String>> regexSplit(final String s, @Language("Regexp") final String regex, final String groupName) {
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
    public static Pair<String, List<String>> replaceAll(final String s, @Language("Regexp") final String regex, final String replacement) {
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
     * @param str The string to check.
     * @param keywords The keywords to look for at the beginning of the string.
     * @return true if the string starts with any of the keywords, false otherwise.
     */
    public static boolean startsWiths(String str, String... keywords) {
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
     * @param str The string to check.
     * @param keywords The keywords to look for at the end of the string.
     * @return true if the string ends with any of the keywords, false otherwise.
     */
    public static boolean endsWiths(String str, String... keywords) {
        for (String keyword : keywords) {
            if (str.endsWith(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given string starts with the specified prefix, ignoring case.
     *
     * @param str The string to check.
     * @param starts The prefix to look for at the beginning of the string.
     * @return true if the string starts with the specified prefix, ignoring case, false otherwise.
     */
    public static boolean startsWithIgnoreCase(String str, String starts) {
        if (starts.length() > str.length()) {
            return false;
        }
        char[] startsChars = starts.toCharArray();
        for (int i = 0; i < startsChars.length; i++) {
            char c = startsChars[i];
            char s = str.charAt(i);
            if (!charEqualIgnoreCase(c, s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the given string ends with the specified suffix, ignoring case.
     *
     * @param str The string to check.
     * @param ends The suffix to look for at the end of the string.
     * @return true if the string ends with the specified suffix, ignoring case, false otherwise.
     */
    public static boolean endsWithIgnoreCase(String str, String ends) {
        if (ends.length() > str.length()) {
            return false;
        }
        char[] endsChars = ends.toCharArray();
        char[] strChars = str.substring(str.length() - endsChars.length).toCharArray();
        for (int i = endsChars.length - 1; i >= 0; i--) {
            char c = endsChars[i];
            char s = strChars[i];
            if (!charEqualIgnoreCase(c, s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given string starts with any of the specified keywords, ignoring case.
     *
     * @param str The string to check.
     * @param keywords The keywords to look for at the beginning of the string.
     * @return true if the string starts with any of the keywords, ignoring case, false otherwise.
     */
    public static boolean startsWithsIgnoreCase(String str, String... keywords) {
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
     * @param str The string to check.
     * @param keywords The keywords to look for at the end of the string.
     * @return true if the string ends with any of the keywords, ignoring case, false otherwise.
     */
    public static boolean endsWithsIgnoreCase(String str, String... keywords) {
        for (String keyword : keywords) {
            if (endsWithIgnoreCase(str, keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches for the first occurrence of the specified target string within the source string, ignoring case.
     *
     * @param source The string to search within.
     * @param target The string to search for.
     * @return The index of the first occurrence of the target string within the source string, or -1 if not found.
     */
    public static int indexOfIgnoreCase(String source, String target) {
        char[] sourceChars = source.toCharArray();
        char[] targetChars = target.toCharArray();
        int targetCount = targetChars.length;
        int sourceCount = sourceChars.length;
        if (targetCount > sourceCount) {
            return -1;
        }
        if (targetCount == 0) {
            return 0;
        }
        char first = targetChars[0];
        int max = sourceCount - targetCount;
        for (int i = 0; i <= max; i++) {
            if (!charEqualIgnoreCase(sourceChars[i], first)) {
                //noinspection StatementWithEmptyBody
                while (++i <= max && !charEqualIgnoreCase(sourceChars[i], first)) ;
            }
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                //noinspection StatementWithEmptyBody
                for (int k = 1; j < end && charEqualIgnoreCase(sourceChars[j], targetChars[k]); j++, k++) ;
                if (j == end) {
                    return i;
                }
            }
        }
        return -1;
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
    public static boolean containsAnyIgnoreCase(String source, String... targets) {
        if (targets.length < 1) {
            return false;
        }
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
    public static boolean containsAny(String source, String... targets) {
        if (targets.length < 1) {
            return false;
        }
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
     * * @param targets an array of strings to be compared with the source
     * @return true if the source string matches any of the target strings, ignoring case; false otherwise
     */
    public static boolean equalsAnyIgnoreCase(String source, String... targets) {
        if (targets.length < 1) {
            return false;
        }
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
     * @param source the string to be compared against the targets
     * @param targets variable number of strings to compare with the source
     * @return true if the source string matches any of the target strings, false otherwise
     */
    public static boolean equalsAny(String source, String... targets) {
        if (targets.length < 1) {
            return false;
        }
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
     * @param source the string to search in
     * * @param targets variable number of strings to find within the source
     * @return true if the source contains all the target strings, ignoring case, false otherwise
     */
    public static boolean containsAllIgnoreCase(String source, String... targets) {
        if (targets.length < 1) {
            return false;
        }
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
     * * @param targets variable number of strings that are expected to be found within the source string
     * @return true if the source string contains all of the target strings, false otherwise
     */
    public static boolean containsAll(String source, String... targets) {
        if (targets.length < 1) {
            return false;
        }
        for (String target : targets) {
            if (!source.contains(target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if two characters are equal ignoring their case.
     *
     * @param a the first character to compare
     * @param b the second character to compare
     * @return true if both characters are the same when case is ignored, false otherwise
     */
    public static boolean charEqualIgnoreCase(char a, char b) {
        if (((a >= 65 && a <= 90) || (a >= 97 && a <= 122)) && ((b >= 65 && b <= 90) || (b >= 97 && b <= 122)) && a != b) {
            return Math.abs(a - b) == 32;
        }
        return a == b;
    }

    /**
     * Check str is null or trimmed str has length or not.
     *
     * @param str string
     * @return true or false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check string literal value is numeric or not.
     *
     * @param numeric string literal value
     * @return true or false
     */
    public static boolean isNumeric(Object numeric) {
        if (numeric == null) {
            return false;
        }
        return numeric.toString().matches(NUMBER_REGEX);
    }

    public static int countOfContainsIgnoreCase(final String str, final String substr) {
        String source = str;
        int count = 0;
        int idx;
        while ((idx = indexOfIgnoreCase(source, substr)) != -1) {
            count++;
            source = source.substring(idx + 1);
        }
        return count;
    }

    public static int countOfContains(final String str, final String substr) {
        String source = str;
        int count = 0;
        int idx;
        while ((idx = source.indexOf(substr)) != -1) {
            count++;
            source = source.substring(idx + 1);
        }
        return count;
    }

    /**
     * Checks if the provided key is composed entirely of digits, indicating it could be used as an index.
     *
     * @param key the string to check
     * @return true if the key is a non-empty string and all characters are digits, false otherwise
     */
    public static boolean isDigit(@NotNull String key) {
        int len = key.length();
        if (len == 0) return false;
        for (int i = 0; i < len; i++) {
            if (!Character.isDigit(key.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Removes empty lines from the provided string content.
     *
     * @param content the string from which empty lines will be removed
     * @return a new string with all empty lines removed
     */
    public static String removeEmptyLine(String content) {
        return content.replaceAll("\\s*\r?\n", NEW_LINE);
    }

    /**
     * Repeats the given string a specified number of times.
     * If the number of times is less than or equal to 0, an empty string is returned.
     *
     * @param str the string to be repeated
     * @param times the number of times to repeat the string
     * @return a new string consisting of the original string repeated multiple times
     */
    public static String repeat(String str, int times) {
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
     * Convert kebab-case to camel-case.
     *
     * @param content kebab-case content
     * @return camel-case content
     */
    public static String camelize(String content) {
        int idx = content.indexOf("-");
        if (idx == -1) return content;
        if (idx + 1 >= content.length()) return content;
        String p = content.substring(idx, idx + 2);
        content = content.replace(p, p.substring(1).toUpperCase());
        return camelize(content);
    }

    /**
     * Encrypts or decrypts the given content using XOR operation with the provided key.
     *
     * @param content the string to be encrypted or decrypted
     * @param key     the key used for encryption or decryption, which should be a non-empty string
     * @return the encrypted or decrypted string based on the input content and key
     */
    public static String xorEncryptDecrypt(String content, String key) {
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
    public static String hash(String content, String algorithm) {
        return hash(content.getBytes(StandardCharsets.UTF_8), algorithm);
    }

    /**
     * Computes the hash of the given content using the specified algorithm.
     *
     * @param content   the byte array to be hashed
     * @param algorithm the name of the hashing algorithm (e.g., "MD5", "SHA-256")
     * @return a string representing the hexadecimal value of the hash
     */
    public static String hash(byte[] content, String algorithm) {
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
