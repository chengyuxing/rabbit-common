package com.github.chengyuxing.common.utils;


import com.github.chengyuxing.common.StringFormatter;
import com.github.chengyuxing.common.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String util.
 */
public final class StringUtil {
    public static final String NUMBER_REGEX = "-?(0|[1-9]\\d*)(\\.\\d+)?";
    public static final String NEW_LINE = "\n";
    public static final String TAB = "\t";
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
    public static Pair<List<String>, List<String>> regexSplit(String s, String regex, String groupName) {
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

    public static boolean startsWiths(String str, String... keywords) {
        boolean ok = false;
        for (String keyword : keywords) {
            if (str.startsWith(keyword)) {
                ok = true;
                break;
            }
        }
        return ok;
    }

    public static boolean endsWiths(String str, String... keywords) {
        boolean ok = false;
        for (String keyword : keywords) {
            if (str.endsWith(keyword)) {
                ok = true;
                break;
            }
        }
        return ok;
    }

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

    public static boolean startsWithsIgnoreCase(String str, String... keywords) {
        boolean ok = false;
        for (String keyword : keywords) {
            if (startsWithIgnoreCase(str, keyword)) {
                ok = true;
                break;
            }
        }
        return ok;
    }

    public static boolean endsWithsIgnoreCase(String str, String... keywords) {
        boolean ok = false;
        for (String keyword : keywords) {
            if (endsWithIgnoreCase(str, keyword)) {
                ok = true;
                break;
            }
        }
        return ok;
    }

    public static String trimStarts(String str, String... starts) {
        if (starts.length < 1) {
            return str;
        }
        boolean again = false;
        for (String s : starts) {
            if (!s.isEmpty() && str.startsWith(s)) {
                str = str.substring(s.length());
                again = true;
            }
        }
        if (again) {
            return trimStarts(str, starts);
        }
        return str;
    }

    public static String trimEnds(String str, String... ends) {
        if (ends.length < 1) {
            return str;
        }
        boolean again = false;
        for (String end : ends) {
            if (!end.isEmpty() && str.endsWith(end)) {
                str = str.substring(0, str.length() - end.length());
                again = true;
            }
        }
        if (again) {
            return trimEnds(str, ends);
        }
        return str;
    }

    public static String trim(String str, String... keywords) {
        return trimStarts(trimEnds(str, keywords), keywords);
    }

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

    public static boolean containsIgnoreCase(String source, String target) {
        return indexOfIgnoreCase(source, target) > -1;
    }

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
        return Objects.isNull(str) || str.trim().isEmpty();
    }

    /**
     * Check string literal value is numeric or not.
     *
     * @param numeric string literal value
     * @return true or false
     */
    public static boolean isNumeric(Object numeric) {
        if (Objects.isNull(numeric)) {
            return false;
        }
        return numeric.toString().matches(NUMBER_REGEX);
    }

    public static int searchIndexUntilNotBlank(String text, int index, boolean reverse) {
        if (reverse) {
            while (index-- > 0) {
                char c = text.charAt(index);
                if (c != '\n' && c != '\t' && c != '\r' && c != ' ') {
                    return index;
                }
            }
            return -1;
        } else {
            int lastIndex = text.length() - 1;
            while (index++ < lastIndex) {
                char c = text.charAt(index);
                if (c != '\n' && c != '\t' && c != '\r' && c != ' ') {
                    return index;
                }
            }
            return lastIndex + 1;
        }
    }

    public static String replaceFirst(String str, String oldValue, String newValue) {
        return Pattern.compile(oldValue, Pattern.LITERAL).matcher(str).replaceFirst(newValue);
    }

    public static String replaceFirstIgnoreCase(String str, String oldValue, String newValue) {
        return Pattern.compile(oldValue, Pattern.LITERAL | Pattern.CASE_INSENSITIVE).matcher(str).replaceFirst(newValue);
    }

    public static String replaceIgnoreCase(String str, String oldValue, String newValue) {
        return Pattern.compile(oldValue, Pattern.LITERAL | Pattern.CASE_INSENSITIVE).matcher(str).replaceAll(newValue);
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

    public static String removeEmptyLine(String content) {
        return content.replaceAll("\\s*\r?\n", NEW_LINE);
    }
}
