package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.utils.StringUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串表达式值比较器<br>
 * <ul>
 *     <li>支持比较的数据类型: {@code null, blank(空白字符串、null、空集合、空数组),true, false, 字符串(''或""), 数字}</li>
 *     <li>支持的比较操作符: {@code >, <, >=, <=, == ,=, !=, <>}</li>
 *     <li>正则：{@code ~ (包含), !~ (不包含)}</li>
 *     <li>正则：{@code @ (匹配), !@ (不匹配)}</li>
 * </ul>
 */
public class Comparators {
    /**
     * 比较
     *
     * @param a  值
     * @param op 运算符
     * @param b  被比较的值
     * @return 比较结果
     * @throws UnsupportedOperationException 如果比较操作符不在预设中
     */
    public static boolean compare(Object a, String op, Object b) {
        switch (op) {
            case "=":
            case "==":
                return equals(a, b);
            case "!=":
            case "<>":
                return !equals(a, b);
            case ">":
            case ">=":
            case "<":
            case "<=":
                return compareNumber(a, op, b);
            case "~":
                return regexPass(a, b, false);
            case "!~":
                return !regexPass(a, b, false);
            case "@":
                return regexPass(a, b, true);
            case "!@":
                return !regexPass(a, b, true);
            default:
                throw new UnsupportedOperationException(String.format("unknown operation for compare: %s %s %s", a, op, b));
        }
    }

    /**
     * 对比数字类型
     *
     * @param a  值
     * @param op 运算符
     * @param b  被比较的值
     * @return 比较结果
     * @throws UnsupportedOperationException 如果比较操作符不在预设中
     * @throws IllegalArgumentException      如果比较数字类型无效
     */
    public static boolean compareNumber(Object a, String op, Object b) {
        if (isBlank(a) && isBlank(b)) {
            return false;
        }
        if (StringUtil.isNumeric(a) && StringUtil.isNumeric(b)) {
            double aN = getNumber(a);
            double bN = getNumber(b);
            switch (op) {
                case "=":
                case "==":
                    return aN == bN;
                case "!=":
                case "<>":
                    return aN != bN;
                case ">":
                    return aN > bN;
                case ">=":
                    return aN >= bN;
                case "<":
                    return aN < bN;
                case "<=":
                    return aN <= bN;
                default:
                    throw new UnsupportedOperationException(String.format("unknown operation for compare: %s %s %s", a, op, b));
            }
        }
        throw new IllegalArgumentException(String.format("invalid compare: %s %s %s, operator '%s' takes 2 numbers.", a, op, b, op));
    }

    /**
     * 验证内容是否满足正则表达式
     *
     * @param content   内容
     * @param regex     正则表达式
     * @param fullMatch 如果为 {@code true} 则进行匹配，否则进行查找
     * @return 是否通过验证
     */
    public static boolean regexPass(Object content, Object regex, boolean fullMatch) {
        if (isString(content) && isString(regex)) {
            Pattern p = Pattern.compile(getString(regex));
            Matcher m = p.matcher(content.toString());
            if (fullMatch) {
                return m.matches();
            }
            return m.find();
        }
        return false;
    }

    /**
     * 比较是否相等
     *
     * @param a 值
     * @param b 被比较值
     * @return 是否相等
     */
    public static boolean equals(Object a, Object b) {
        if (isBlank(a) && isBlank(b)) {
            return true;
        }
        if (isTrue(a) && isTrue(b)) {
            return true;
        }
        if (isFalse(a) && isFalse(b)) {
            return true;
        }
        if (StringUtil.isNumeric(a) && StringUtil.isNumeric(b)) {
            return Objects.equals(getNumber(a), getNumber(b));
        }
        if (isString(a) && isString(b)) {
            return Objects.equals(getString(a), getString(b));
        }
        return false;
    }

    /**
     * 将值转换为便于比较的类型
     *
     * @param a 值
     * @return 可进行比较的值
     */
    public static Object valueOf(Object a) {
        if (a == null) {
            return ValueType.NULL;
        }
        if (isString(a)) {
            String v = a.toString().trim();
            if (v.isEmpty()) {
                return ValueType.BLANK;
            }
            if (v.equalsIgnoreCase("true")) {
                return ValueType.TRUE;
            }
            if (v.equalsIgnoreCase("false")) {
                return ValueType.FALSE;
            }
            if (v.equalsIgnoreCase("null")) {
                return ValueType.NULL;
            }
            if (v.equalsIgnoreCase("blank")) {
                return ValueType.BLANK;
            }
        }
        return a;
    }

    /**
     * 排除引号获取字符串
     *
     * @param value 字符串
     * @return 排除引号后的字符串
     */
    public static String getString(Object value) {
        if (value == null) {
            return null;
        }
        String s = value.toString();
        if (isQuote(s)) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * 获取数字值
     *
     * @param value 可转数字的值
     * @return 数字值
     */
    public static Double getNumber(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        return Double.parseDouble(value.toString());
    }

    public static boolean isQuote(String s) {
        if (s.length() < 2) {
            return false;
        }
        return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
    }

    /**
     * 数据值类型
     */
    public enum ValueType {
        NULL("null"), BLANK("blank"), FALSE("false"), TRUE("true");
        private final String value;

        ValueType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static boolean isString(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return true;
        }
        String s = value.toString();
        return isQuote(s);
    }

    public static boolean isNull(Object value) {
        if (value == null) {
            return true;
        }
        return value == ValueType.NULL;
    }

    public static boolean isBlank(Object value) {
        if (isNull(value)) {
            return true;
        }
        if (value == ValueType.BLANK) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Object[]) {
            return ((Object[]) value).length == 0;
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        return false;
    }

    public static boolean isTrue(Object value) {
        if (value == null) {
            return false;
        }
        if (value == ValueType.TRUE) {
            return true;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    public static boolean isFalse(Object value) {
        if (value == null) {
            return false;
        }
        if (value == ValueType.FALSE) {
            return true;
        }
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        return false;
    }
}
