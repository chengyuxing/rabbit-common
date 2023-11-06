package com.github.chengyuxing.common;

import com.github.chengyuxing.common.script.Patterns;
import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
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
    private final Pattern pattern = Pattern.compile("\\$\\{\\s*(?<key>!?" + Patterns.VAR_KEY_PATTERN + ")\\s*}");

    /**
     * Format string template with variable map.
     * e.g.
     * <blockquote>
     * <pre>template: select ${ fields } from test.user where ${  cnd} and id in (${!idArr}) or id = ${!idArr.1}</pre>
     * <pre>variables: {fields: "id, name", cnd: "name = 'cyx'", idArr: ["a", "b", "c"]}</pre>
     * <pre>result: select id, name from test.user where name = 'cyx' and id in ('a', 'b', 'c') or id = 'b'</pre>
     * </blockquote>
     *
     * @param template string template
     * @param data     variables
     * @return formatted string template
     */
    public String format(final String template, final Map<String, ?> data) {
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
        return doFormat(template, data);
    }

    /**
     * Do format string template with variable map.
     *
     * @param template string template
     * @param data     variables
     * @return formatted string template
     */
    protected String doFormat(final String template, final Map<String, ?> data) {
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
                        value = parseValue(key, ObjectUtil.getDeepValue(data, key), isSpecial);
                    } else {
                        value = parseValue(key, data.get(key), isSpecial);
                    }
                    copy = copy.replace(holder, value);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return doFormat(copy, data);
        }
        if (copy.lastIndexOf(TEMP_HOLDER_PREFIX) != -1) {
            copy = copy.replace(TEMP_HOLDER_PREFIX, DEFAULT_HOLDER_PREFIX);
        }
        return copy;
    }

    /**
     * Parse object value to string literal value.
     *
     * @param key       key
     * @param value     value
     * @param isSpecial key name starts with '{@code !}' or not
     * @return string literal value
     */
    protected String parseValue(String key, Object value, boolean isSpecial) {
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
