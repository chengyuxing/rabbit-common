package com.github.chengyuxing.common;

import com.github.chengyuxing.common.script.expression.Patterns;
import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String template formatter.
 */
public class StringFormatter {
    private static final char DEFAULT_HOLDER_PREFIX = '$';
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final char TEMP_HOLDER_PREFIX = '\u0c32';
    //language=RegExp
    private static final Pattern pattern = Pattern.compile("\\$\\{\\s*(?<key>!?" + Patterns.VAR_KEY_PATTERN + ")\\s*}");

    /**
     * Format string template with a variable map.
     * <p>e.g. template: </p>
     * <blockquote>
     * <pre>select ${ fields } from test.user
     * where ${  cnd}
     * and id in (${!idArr})
     * or id = ${!idArr.1}</pre>
     * </blockquote>
     * <p>variables: </p>
     * <blockquote>
     * <pre>{
     * fields: "id, name",
     * cnd: "name = 'cyx'",
     * idArr: ["a", "b", "c"]
     * }</pre>
     * </blockquote>
     * <p>result: </p>
     * <blockquote>
     * <pre>select id, name from test.user
     * where name = 'cyx'
     * and id in ('a', 'b', 'c')
     * or id = 'b'</pre>
     * </blockquote>
     *
     * @param template       string template
     * @param data           variables
     * @param valueFormatter function for parse object value to string literal value (value, key name starts with '{@code !}' or not) -&gt; string literal value.
     * @return formatted string template
     */
    public String format(final String template, final Map<String, ?> data, BiFunction<Object, Boolean, String> valueFormatter) {
        if (Objects.isNull(template)) {
            return "";
        }
        if (template.trim().isEmpty()) {
            return template;
        }
        if (!template.contains("${")) {
            return template;
        }
        if (Objects.isNull(data) || data.isEmpty()) {
            return template;
        }
        return doFormat(template, data, valueFormatter);
    }

    /**
     * Format string template with variable map.
     * <p>If key starts with {@code !}, do quote with {@code ''}.</p>
     * <p>e.g. template: </p>
     * <blockquote>
     * <pre>select ${ fields } from test.user
     * where ${  cnd}
     * and id in (${!idArr})
     * or id = ${!idArr.1}</pre>
     * </blockquote>
     * <p>variables: </p>
     * <blockquote>
     * <pre>{
     * fields: "id, name",
     * cnd: "name = 'cyx'",
     * idArr: ["a", "b", "c"]
     * }</pre>
     * </blockquote>
     * <p>result: </p>
     * <blockquote>
     * <pre>select id, name from test.user
     * where name = 'cyx'
     * and id in ('a', 'b', 'c')
     * or id = 'b'</pre>
     * </blockquote>
     *
     * @param template string template
     * @param data     variables
     * @return formatted string template
     */
    public String format(String template, Map<String, ?> data) {
        return format(template, data, (value, isSpecial) -> {
            if (value == null) {
                return "";
            }
            Object[] values = ObjectUtil.toArray(value);
            StringJoiner sb = new StringJoiner(", ");
            for (Object v : values) {
                if (v != null) {
                    String s = v.toString();
                    if (isSpecial) {
                        s = "'" + s + "'";
                    }
                    sb.add(s);
                }
            }
            return sb.toString();
        });
    }

    /**
     * Do format string template with variable map.
     *
     * @param template       string template
     * @param data           variables
     * @param valueFormatter value formatter
     * @return formatted string template
     */
    protected String doFormat(final String template, final Map<String, ?> data, BiFunction<Object, Boolean, String> valueFormatter) {
        String copy = template;
        Matcher m = getPattern().matcher(copy);
        if (m.find()) {
            // full str template key e.g. ${ !myKey  }
            String holder = m.group(0);
            // real key e.g. !myKey
            String key = m.group("key");
            boolean isSpecial = key.startsWith("!");
            if (isSpecial) {
                key = key.substring(1);
            }
            // e.g. user.cats.1
            // template var key contains dot but data not contains this key, then be key-path otherwise normal key.
            boolean isKeyPath = key.contains(".") && !data.containsKey(key);
            String dataKey = isKeyPath ? key.substring(0, key.indexOf(".")) : key;
            if (!data.containsKey(dataKey)) {
                copy = copy.replace(holder, TEMP_HOLDER_PREFIX + holder.substring(1));
            } else {
                try {
                    String value;
                    if (isKeyPath) {
                        value = valueFormatter.apply(ObjectUtil.getDeepValue(data, key), isSpecial);
                    } else {
                        value = valueFormatter.apply(data.get(key), isSpecial);
                    }
                    copy = copy.replace(holder, value);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return doFormat(copy, data, valueFormatter);
        }
        if (copy.lastIndexOf(TEMP_HOLDER_PREFIX) != -1) {
            copy = copy.replace(TEMP_HOLDER_PREFIX, DEFAULT_HOLDER_PREFIX);
        }
        return copy;
    }

    /**
     * Get string template variable pattern.
     *
     * @return string template variable pattern
     */
    public Pattern getPattern() {
        return pattern;
    }
}
