package com.github.chengyuxing.common.utils;


import com.github.chengyuxing.common.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class StringUtil {
    public static final Pattern STR_TEMP_PATTERN = Pattern.compile("\\$\\{\\s*(?<key>[\\w._-]+)\\s*}");
    public static final String NUMBER_REGEX = "-?(0|[1-9]\\d*)(\\.\\d+)?";

    public static final String NEW_LINE = "\n";
    public static final String TAB = "\t";

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
     * 判断被查询的字符串是否包含其中一个目标字符串
     *
     * @param source  被查询的字符串
     * @param targets 被包含的字符串组
     * @return 是否包含任意一个
     */
    public static boolean containsAny(String source, String... targets) {
        if (targets.length < 1) {
            return true;
        }
        for (String target : targets) {
            if (source.contains(target)) {
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
    public static boolean equalsAnyIgnoreCase(String source, String... targets) {
        if (targets.length < 1) {
            return true;
        }
        for (String target : targets) {
            if (source.equalsIgnoreCase(target)) {
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
     * 判断被查询的字符串是否包含全部目标字符串
     *
     * @param source  被查询的字符串
     * @param targets 被包含的字符串组
     * @return 是否包含全部
     */
    public static boolean containsAll(String source, String... targets) {
        if (targets.length < 1) {
            return true;
        }
        for (String target : targets) {
            if (!source.contains(target)) {
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
        if (((a >= 65 && a <= 90) || (a >= 97 && a <= 122)) && ((b >= 65 && b <= 90) || (b >= 97 && b <= 122)) && a != b) {
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

    /**
     * 判断是否是数字
     *
     * @param numeric 字符串数字
     * @return 是否是数字
     */
    public static boolean isNumeric(Object numeric) {
        if (numeric == null) {
            return false;
        }
        return numeric.toString().matches(NUMBER_REGEX);
    }

    /**
     * 从一个指定索引开始查找一段字符串内容直到不为空白字符
     *
     * @param text    内容
     * @param index   起始索引
     * @param reverse 是否反向查找
     * @return 不为空白字符的索引，如果为-1，说明第一个字符是空白，如果为字符串的长度，则说明最后一个字符也是空白
     */
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

    /**
     * 忽略大小写查询子字符在字符串中串出现的次数
     *
     * @param str    字符串
     * @param substr 子字符串
     * @return 出现次数
     */
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

    /**
     * 替换
     *
     * @param str      字符串
     * @param oldValue 需要被替换的旧值
     * @param newValue 需要替换的旧值
     * @return 替换后的字符串
     */
    public static String replaceFirst(String str, String oldValue, String newValue) {
        if (oldValue.equals(newValue)) {
            return str;
        }
        int index = str.indexOf(oldValue);
        if (index == -1) {
            return str;
        }
        String left = str.substring(0, index);
        String right = str.substring(index + oldValue.length());
        return left + newValue + right;
    }

    /**
     * 忽略大小写替换
     *
     * @param str      字符串
     * @param oldValue 需要被替换的旧值
     * @param newValue 需要替换的旧值
     * @return 替换后的字符串
     */
    public static String replaceIgnoreCase(String str, String oldValue, String newValue) {
        if (oldValue.equalsIgnoreCase(newValue)) {
            return str;
        }
        int index = indexOfIgnoreCase(str, oldValue);
        if (index == -1) {
            return str;
        }
        String left = str.substring(0, index);
        String right = str.substring(index + oldValue.length());
        String replacedFirst = left + newValue + right;
        return replaceIgnoreCase(replacedFirst, oldValue, newValue);
    }

    /**
     * 忽略大小写替换第一个匹配项
     *
     * @param str      字符串
     * @param oldValue 需要被替换的旧值
     * @param newValue 需要替换的旧值
     * @return 替换后的字符串
     */
    public static String replaceFirstIgnoreCase(String str, String oldValue, String newValue) {
        if (oldValue.equalsIgnoreCase(newValue)) {
            return str;
        }
        int index = indexOfIgnoreCase(str, oldValue);
        if (index == -1) {
            return str;
        }
        String left = str.substring(0, index);
        String right = str.substring(index + oldValue.length());
        return left + newValue + right;
    }

    /**
     * 字符串格式化<br>
     * <blockquote>
     * e.g.
     * <pre>参数：{name: "world", days: ["Mon", "Tue", "Thr"]}</pre>
     * <pre>字符串：Hello ${name} ${days.0}!</pre>
     * <pre>输出：Hello world Mon!</pre>
     * </blockquote>
     *
     * @param str       字符串
     * @param args      参数
     * @param formatter 值格式化函数
     * @return 格式化后的字符串
     */
    public static String format(String str, Map<String, Object> args, Function<Object, String> formatter) {
        if (args.isEmpty()) {
            return str;
        }
        String res = str;
        try {
            Matcher m = STR_TEMP_PATTERN.matcher(str);
            while (m.find()) {
                String keyTemp = m.group(0);
                String keyPath = m.group("key");
                if (args.containsKey(keyPath)) {
                    res = res.replace(keyTemp, formatter.apply(args.get(keyPath)));
                } else {
                    int dotIdx = keyPath.indexOf(".");
                    if (dotIdx != -1) {
                        String key = keyPath.substring(0, dotIdx);
                        if (args.containsKey(key)) {
                            Object v = ObjectUtil.getDeepNestValue(args, "/" + keyPath.replace(".", "/"));
                            res = res.replace(keyTemp, formatter.apply(v));
                        }
                    }
                }
            }
            return res;
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 字符串格式化<br>
     * <blockquote>
     * e.g.
     * <pre>参数：{name: "world", days: ["Mon", "Tue", "Thr"]}</pre>
     * <pre>字符串：Hello ${name} ${days.0}!</pre>
     * <pre>输出：Hello world Mon!</pre>
     * </blockquote>
     *
     * @param str  字符串
     * @param args 参数
     * @return 格式化后的字符串
     */
    public static String format(String str, Map<String, Object> args) {
        return format(str, args, o -> {
            if (o == null) {
                return "";
            }
            return o.toString();
        });
    }

    /**
     * 字符串格式化<br>
     * <blockquote>
     * e.g.
     * <pre>键：name</pre>
     * <pre>值：World</pre>
     * <pre>字符串：Hello ${ name}!</pre>
     * <pre>输出：Hello World!</pre>
     * </blockquote>
     *
     * @param str   字符串
     * @param key   键
     * @param value 值
     * @return 格式化后的字符串
     */
    public static String format(String str, String key, Object value) {
        String res = str;
        Matcher m = STR_TEMP_PATTERN.matcher(str);
        while (m.find()) {
            if (Objects.equals(m.group("key"), key.trim())) {
                Object v = value;
                if (v == null) {
                    v = "";
                }
                res = res.replace(m.group(0), v.toString());
            }
        }
        return res;
    }

    /**
     * 查询子字符在字符串中串出现的次数
     *
     * @param str    字符串
     * @param substr 子字符串
     * @return 出现次数
     */
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
     * 获取字节数组对象的大小
     *
     * @param bytes 字节数组
     * @return 文件大小
     */
    public static String getSize(byte[] bytes) {
        String strSize = "0KB";
        final Formatter fmt = new Formatter();
        if (bytes.length > 1048576) {
            strSize = fmt.format("%.2f", bytes.length / 1048576.0) + "MB";
        } else if (bytes.length > 0) {
            strSize = fmt.format("%.2f", bytes.length / 1024.0) + "KB";
        }
        return strSize;
    }
}
