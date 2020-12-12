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

    public static boolean startsWiths(String str, String[] keywords) {
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
     * 判断字符串是否有长度
     *
     * @param str 字符串
     * @return 是否有长度
     */
    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }
}
