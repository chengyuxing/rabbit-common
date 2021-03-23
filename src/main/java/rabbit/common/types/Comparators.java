package rabbit.common.types;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串表达式值比较器<br>
 * 支持比较的数据类型: {@code null, blank(空白字符串和null),true, false, 字符串(''或""), 数字}<br>
 * 支持的比较操作符: {@code >, <, >=, <=, == (=), != (<>)}<br>
 * {@code ~ (正则查找包含), !~ (正则查找不包含)}<br>
 * {@code @ (正则匹配), !@ (正则不匹配)}<br>
 */
public class Comparators {
    public static final String NUMBER_REGEX = "-?([0-9]|(0\\.\\d+)|([1-9]+\\.?\\d+))";

    /**
     * 值对比
     *
     * @param name  对应参数字典的key名
     * @param op    操作符
     * @param value 被比对的值
     * @param args  参数字典
     * @return 比较结果
     */
    public static boolean compare(String name, String op, String value, Map<String, Object> args) {
        Object source = args.get(name);
        if (op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")) {
            if (source == null) {
                return false;
            }
            if (source.toString().matches(NUMBER_REGEX) && value.matches(NUMBER_REGEX)) {
                return compareNumber(name, op, value, args);
            }
            throw new UnsupportedOperationException(String.format("can not compare NonNumber: %s %s %s", name, op, value));
        }
        return compareNonNumber(name, op, value, args);
    }

    /**
     * 对比数字类型
     *
     * @param name  对应参数字典的key名
     * @param op    操作符
     * @param value 被比对的值
     * @param args  参数字典
     * @return 比较结果
     */
    public static boolean compareNumber(String name, String op, String value, Map<String, Object> args) {
        double targetNum = Double.parseDouble(value);
        double sourceNum = Double.parseDouble(args.get(name).toString());
        switch (op) {
            case "=":
            case "==":
                return sourceNum == targetNum;
            case ">":
                return sourceNum > targetNum;
            case "<":
                return sourceNum < targetNum;
            case ">=":
                return sourceNum >= targetNum;
            case "<=":
                return sourceNum <= targetNum;
            case "!=":
            case "<>":
                return sourceNum != targetNum;
            default:
                throw new UnsupportedOperationException(String.format("unKnow operation of child expression: %s %s %s", name, op, value));
        }
    }

    /**
     * 对比非数字类型
     *
     * @param name  对应参数字典的key名
     * @param op    操作符
     * @param value 被比对的值
     * @param args  参数字典
     * @return 比较结果
     */
    public static boolean compareNonNumber(String name, String op, String value, Map<String, Object> args) {
        Object source = args.get(name);
        switch (op) {
            case "=":
            case "==":
                return equal(source, value);
            case "!=":
            case "<>":
                return !equal(source, value);
            case "~":
                return regexEqual(source, value, false);
            case "!~":
                return !regexEqual(source, value, false);
            case "@":
                return regexEqual(source, value, true);
            case "!@":
                return !regexEqual(source, value, true);
            default:
                throw new UnsupportedOperationException(String.format("unKnow operation for compare NonNumber: \"%s\" %s %s", source, op, value));
        }
    }

    /**
     * 正则表达式比较
     *
     * @param source    源值
     * @param value     正则表达式
     * @param fullMatch 是否全匹配，否则就是包含关系
     * @return 是否满足正则
     */
    public static boolean regexEqual(Object source, String value, boolean fullMatch) {
        if (source == null) {
            return false;
        }
        String regex = getString(value);
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(source.toString());
        if (fullMatch) {
            return m.matches();
        }
        return m.find();
    }

    /**
     * 比对相等
     *
     * @param source 源值
     * @param value  被比对的值
     * @return 是否相等
     */
    public static boolean equal(Object source, String value) {
        if (value.equals("null")) {
            return source == null;
        }
        if (value.equals("blank")) {
            return source == null || "".equals(source.toString().trim());
        }
        if (value.equals("true")) {
            return source.equals(true);
        }
        if (value.equals("false")) {
            return source.equals(false);
        }
        if (source == null) {
            return false;
        }
        return getString(value).equals(source.toString());
    }

    /**
     * 排除引号获取字符串
     *
     * @param value 字符串
     * @return 排除引号后的字符串
     */
    private static String getString(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
