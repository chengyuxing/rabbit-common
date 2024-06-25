package com.github.chengyuxing.common.script.expression;

import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h2>String literal value comparator</h2>
 * <p>support value type: </p>
 * <blockquote><pre>null, blank('', "", null, &lt;empty collection&gt;, []), true, false, string('...' or "..."), number(1, 3.14)
 * </pre></blockquote>
 * <p>support compare operator: </p>
 * <blockquote><pre>
 *     &gt;, &lt;, &gt;=, &lt;=, == ,=, !=, &lt;&gt;
 * </pre></blockquote>
 * <p>support regex operator: </p>
 * <blockquote><pre>
 * ~ (contains),
 * !~ (not contains)
 * {@code @} (match)
 * !@ (not match)
 * </pre></blockquote>
 */
public class Comparators {
    /**
     * Compare two object.
     *
     * @param a  a
     * @param op operator
     * @param b  b
     * @return true or false
     * @throws UnsupportedOperationException if operator not exists
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
     * Compare two number.
     *
     * @param a  a
     * @param op operator
     * @param b  b
     * @return true or false
     * @throws UnsupportedOperationException if operator not exists
     * @throws IllegalArgumentException      if a or b not a number
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
     * Regex result is true or false.
     *
     * @param content   content
     * @param regex     regex
     * @param fullMatch true: {@link Matcher#matches()}, false: {@link Matcher#find()}
     * @return true or false
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
     * Compare equals.
     *
     * @param a a
     * @param b b
     * @return true if equals or false
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
     * Convert value to boxed.
     *
     * @param a value
     * @return boxed value
     */
    public static Object valueOf(Object a) {
        if (a == null) {
            return ValueType.NULL;
        }
        if (isString(a)) {
            String v = a.toString().trim();
            if (isQuote(v)) {
                return getString(v);
            }
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
     * Exclude quotes and get.
     *
     * @param value string value
     * @return value without outer quotes
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
     * Convert to double and get.
     *
     * @param value string literal number value
     * @return double value
     */
    public static Double getNumber(Object value) {
        return ObjectUtil.toDouble(value);
    }

    /**
     * Boxed keyword value type.
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

    public static boolean isQuote(String s) {
        if (s.length() < 2) {
            return false;
        }
        return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
    }

    public static boolean isString(Object value) {
        if (value == null) {
            return false;
        }
        return value instanceof String;
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
