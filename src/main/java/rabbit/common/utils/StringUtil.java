package rabbit.common.utils;

/**
 * 字符串工具类
 */
public class StringUtil {
    /**
     * 去除字符串中多余的换行符和空格
     *
     * @param str 字符串
     * @return 字符间保留一个空格的字符串
     */
    public static String moveSpecialChars(final String str) {
        return str.replaceAll("[\\s\n\r]+", " ");
    }
}
