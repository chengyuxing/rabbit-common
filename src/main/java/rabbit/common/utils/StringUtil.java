package rabbit.common.utils;

import rabbit.common.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class StringUtil {
    /**
     * 从指定开始字符串开始向前查找
     * 从第一个非(空格\n\t)字符开始往前找第一个指定关键字，是否包含指定的关键字
     *
     * @param str     字符串
     * @param from    开始字符串
     * @param keyword 要查找的关键字
     * @return 是否包含
     */
    public static boolean prevKeywordContains(String str, String from, String keyword) {
        int idx = str.indexOf(from) - 1;
        int len = keyword.length();
        StringBuilder sb = new StringBuilder();
        int x = 0;
        for (int i = idx; i > 0; i--) {
            char c = str.charAt(i);
            if (x > 0) {
                sb.insert(0, c);
                x++;
                if (x == len) {
                    break;
                }
                continue;
            }
            if (c != ' ' && c != '\n' && c != '\t') {
                sb.insert(0, c);
                x++;
            }
        }
        return sb.toString().equals(keyword);
    }

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
    public static Pair<List<String>, List<String>> split(String s, String regex, String groupName) {
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
}
