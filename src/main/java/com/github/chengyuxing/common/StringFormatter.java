package com.github.chengyuxing.common;

import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串格式化器
 */
public class StringFormatter {
    private static final char DEFAULT_HOLDER_PREFIX = '$';
    private static final char TEMP_HOLDER_PREFIX = '\u0c32';
    private final Pattern pattern = Pattern.compile("\\$\\{\\s*(?<key>:?[\\w._-]+)\\s*}");

    /**
     * 格式化字符串模版
     * e.g.
     * <blockquote>
     * <pre>字符串：select ${ fields } from test.user where ${  cnd} and id in (${:idArr}) or id = ${:idArr.1}</pre>
     * <pre>参数：{fields: "id, name", cnd: "name = 'cyx'", idArr: ["a", "b", "c"]}</pre>
     * <pre>结果：select id, name from test.user where name = 'cyx' and id in ('a', 'b', 'c') or id = 'b'</pre>
     * </blockquote>
     *
     * @param template 字符串模版
     * @param data     数据
     * @return 格式化后的字符串
     */
    public String format(String template, Map<String, ?> data) {
        if (Objects.isNull(template)) {
            return "";
        }
        if (template.trim().isEmpty()) {
            return template;
        }
        if (Objects.isNull(data) || data.isEmpty()) {
            return template;
        }
        String copy = template;
        Matcher m = pattern.matcher(copy);
        if (m.find()) {
            // full str template key e.g. ${ :myKey  }
            String holder = m.group(0);
            // real key e.g. :myKey
            String key = m.group("key");
            boolean isSpecial = key.startsWith(":");
            if (isSpecial) {
                key = key.substring(1);
            }
            // e.g. user.cats.1
            // 字符串中有键路径，但参数中没有此路径键，才默认是键路径表达式，否分，默认args有名为此路径的键
            boolean isKeyPath = key.contains(".") && !data.containsKey(key);
            String dataKey = isKeyPath ? key.substring(0, key.indexOf(".")) : key;
            if (!data.containsKey(dataKey)) {
                copy = copy.replace(holder, TEMP_HOLDER_PREFIX + holder.substring(1));
            } else {
                try {
                    String value;
                    if (isKeyPath) {
                        value = parseValue(ObjectUtil.getDeepValue(data, key), isSpecial);
                    } else {
                        value = parseValue(data.get(key), isSpecial);
                    }
                    copy = copy.replace(holder, value);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return format(copy, data);
        }
        if (copy.lastIndexOf(TEMP_HOLDER_PREFIX) != -1) {
            copy = copy.replace(TEMP_HOLDER_PREFIX, DEFAULT_HOLDER_PREFIX);
        }
        return copy;
    }

    /**
     * 解析值为替换占位符的字符串
     *
     * @param value     占位符对应的数据值
     * @param isSpecial 占位符键名是否有特殊前缀
     * @return 替换的字符串
     */
    protected String parseValue(Object value, boolean isSpecial) {
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

    public Pattern getPattern() {
        return pattern;
    }
}
