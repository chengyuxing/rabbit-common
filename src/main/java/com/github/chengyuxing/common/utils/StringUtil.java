package com.github.chengyuxing.common.utils;

import com.github.chengyuxing.common.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class StringUtil {

    /**
     * 根据正则表达式所匹配的分组分割字符串<br>
     * 例如一个字符串 {@code aaa##bbb%%ddd}<br>
     * RegexResult
     * <blockquote>
     * {@code (?<symbol>##|%%)} symbol :   { [ aaa, bbb, ccc] , [ ## , %% ] } <br>
     *
     * </blockquote>
     *
     * @param s         字符串
     * @param regex     正则表达式
     * @param groupName 组名
     * @return 二元组(分割后的字符串列表, 匹配的分割符号列表)
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

    /**
     * 判断一个字符串的开头是否出现在一组关键字中
     *
     * @param str      字符串
     * @param keywords 一组关键字
     * @return 是否匹配
     */
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

    /**
     * 判断一个字符串的开头是否出现在一组关键字中
     *
     * @param str      字符串
     * @param keywords 一组关键字
     * @return 是否匹配
     */
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

    /**
     * 忽略大小写判断一个字符串是否以指定的子字符串开头
     *
     * @param str    字符串
     * @param starts 开头子字符串
     * @return 是否匹配
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
     * 忽略大小写判断一个字符串是否以指定的子字符串结尾
     *
     * @param str  字符串
     * @param ends 结尾子字符串
     * @return 是否匹配
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
     * 忽略大小写判断一个字符串的开头是否出现在一组关键字中
     *
     * @param str      字符串
     * @param keywords 一组关键字
     * @return 是否匹配
     */
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

    /**
     * 忽略大小写判断一个字符串的结尾是否出现在一组关键字中
     *
     * @param str      字符串
     * @param keywords 一组关键字
     * @return 是否匹配
     */
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

    /**
     * 截断以一组关键字起始的指定字符串开头
     *
     * @param str    字符串
     * @param starts 开头中起始字符串所出现的一组关键字
     * @return 截断后的字符串
     */
    public static String trimStarts(String str, String... starts) {
        if (starts.length < 1) {
            return str;
        }
        boolean again = false;
        for (String s : starts) {
            if (!s.equals("") && str.startsWith(s)) {
                str = str.substring(s.length());
                again = true;
            }
        }
        if (again) {
            return trimStarts(str, starts);
        }
        return str;
    }

    /**
     * 截断以一组关键字起始的指定字符串结尾
     *
     * @param str  字符串
     * @param ends 结尾中起始字符串所出现的一组关键字
     * @return 截断后的字符串
     */
    public static String trimEnds(String str, String... ends) {
        if (ends.length < 1) {
            return str;
        }
        boolean again = false;
        for (String end : ends) {
            if (!end.equals("") && str.endsWith(end)) {
                str = str.substring(0, str.length() - end.length());
                again = true;
            }
        }
        if (again) {
            return trimEnds(str, ends);
        }
        return str;
    }

    /**
     * 截断以一组关键字起始的指定字符串头和尾
     *
     * @param str      字符串
     * @param keywords 开头和结尾中起始字符串所出现的一组关键字
     * @return 截断后的字符串
     */
    public static String trim(String str, String... keywords) {
        return trimStarts(trimEnds(str, keywords), keywords);
    }

    /**
     * 忽略大小写获取子字符串在指定字符串中的索引
     *
     * @param source 被查询的字符串
     * @param target 要查询的字符串
     * @return 索引
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
     * 忽略大小写判断被查询的字符串是否包含目标字符串
     *
     * @param source 被查询的字符串
     * @param target 被包含的字符串
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(String source, String target) {
        return indexOfIgnoreCase(source, target) > -1;
    }

    /**
     * 忽略大小写判断被查询的字符串是否匹配目标字符串
     *
     * @param source 被查询的字符串
     * @param target 被包含的字符串
     * @return 是否匹配
     */
    public static boolean matchesIgnoreCase(String source, String target) {
        return indexOfIgnoreCase(source, target) > -1 && source.length() == target.length();
    }

    /**
     * 忽略大小写判断被查询的字符串是否包含其中一个目标字符串
     *
     * @param source  被查询的字符串
     * @param targets 被包含的字符串组
     * @return 是否包含任意一个
     */
    public static boolean containsAnyIgnoreCase(String source, String... targets) {
        if (targets.length < 1) {
            return true;
        }
        for (String target : targets) {
            if (containsIgnoreCase(source, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 忽略大小写判断被查询的字符串是否匹配其中一个目标字符串
     *
     * @param source  被查询的字符串
     * @param targets 被包含的字符串组
     * @return 是否包含任意一个
     */
    public static boolean matchesAnyIgnoreCase(String source, String... targets) {
        if (targets.length < 1) {
            return true;
        }
        for (String target : targets) {
            if (matchesIgnoreCase(source, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 忽略大小写判断被查询的字符串是否包含全部目标字符串
     *
     * @param source  被查询的字符串
     * @param targets 被包含的字符串组
     * @return 是否包含全部
     */
    public static boolean containsAllIgnoreCase(String source, String... targets) {
        if (targets.length < 1) {
            return true;
        }
        for (String target : targets) {
            if (!containsIgnoreCase(source, target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 忽略大小写判断被查询的字符串是否包含全部目标字符串
     *
     * @param source  被查询的字符串
     * @param targets 被包含的字符串组
     * @return 是否包含全部
     */
    public static boolean matchesAllIgnoreCase(String source, String... targets) {
        if (targets.length < 1) {
            return true;
        }
        for (String target : targets) {
            if (!matchesIgnoreCase(source, target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 忽略大小写判断两个字符是否相等
     *
     * @param a 被比较字符
     * @param b 待比较字符
     * @return 是否相等
     */
    public static boolean charEqualIgnoreCase(char a, char b) {
        if (a >= 65 && a <= 122 && b >= 65 && b <= 122 && a != b) {
            return Math.abs(a - b) == 32;
        }
        return a == b;
    }

    /**
     * 判断字符串是否有长度
     *
     * @param str 字符串
     * @return 是否有长度
     */
    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }
}
