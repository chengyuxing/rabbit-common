package rabbit.common.utils;

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
}
