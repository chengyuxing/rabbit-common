package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
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
public final class Comparators {
    /**
     * Compare two object.
     *
     * @param a  a
     * @param op operator
     * @param b  b
     * @return true or false
     * @throws UnsupportedOperationException if operator not exists
     */
    public static boolean compare(Object a, @NotNull String op, Object b) {
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
                return regexTest(a, b, false);
            case "!~":
                return !regexTest(a, b, false);
            case "@":
                return regexTest(a, b, true);
            case "!@":
                return !regexTest(a, b, true);
            default:
                throw new UnsupportedOperationException(String.format("unknown operation for compare: %s %s %s", a, op, b));
        }
    }

    /**
     * Compare two numbers.
     *
     * @param a  a
     * @param op operator
     * @param b  b
     * @return true or false
     */
    public static boolean compareNumber(Object a, String op, Object b) {
        if (StringUtils.isNumber(a) && StringUtils.isNumber(b)) {
            BigDecimal aN = new BigDecimal(a.toString());
            BigDecimal bN = new BigDecimal(b.toString());
            switch (op) {
                case ">":
                    return aN.compareTo(bN) > 0;
                case ">=":
                    return aN.compareTo(bN) >= 0;
                case "<":
                    return aN.compareTo(bN) < 0;
                case "<=":
                    return aN.compareTo(bN) <= 0;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + op);
            }
        }
        throw new IllegalArgumentException(String.format("Invalid compare: %s %s %s, operator '%s' takes 2 numbers.", a, op, b, op));
    }

    /**
     * Regex result is true or false.
     *
     * @param content   content
     * @param regex     regex
     * @param fullMatch true: {@link Matcher#matches()}, false: {@link Matcher#find()}
     * @return true or false
     */
    public static boolean regexTest(Object content, Object regex, boolean fullMatch) {
        if (isString(content) && isString(regex)) {
            Pattern p = Pattern.compile(regex.toString());
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
        return Objects.equals(a.toString(), b.toString());
    }

    public static boolean isString(Object value) {
        if (value == null) {
            return false;
        }
        return value instanceof String;
    }

    public static boolean isBlank(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return StringUtils.isBlank((String) value);
        }
        if (value instanceof Collection<?>) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Iterable<?>) {
            return ((Iterable<?>) value).iterator().hasNext();
        }
        if (value instanceof Object[]) {
            return ((Object[]) value).length == 0;
        }
        if (value instanceof Map<?, ?>) {
            return ((Map<?, ?>) value).isEmpty();
        }
        return false;
    }
}
