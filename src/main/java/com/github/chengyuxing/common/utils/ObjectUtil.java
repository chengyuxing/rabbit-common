package com.github.chengyuxing.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 简单基本对象工具类
 */
public final class ObjectUtil {
    /**
     * 不限长赋值表达式<br>
     * 逻辑形如: if(a==b) return v1 else if(a==c) return v2 else if(a==d) return v3 (可选)全部匹配不到的默认值: else return v4
     *
     * @param value  值
     * @param equal  比较值
     * @param result 结果
     * @param more   更多
     * @return 结果
     */
    public static Object decode(Object value, Object equal, Object result, Object... more) {
        Object[] objs = new Object[more.length + 2];
        objs[0] = equal;
        objs[1] = result;
        System.arraycopy(more, 0, objs, 2, more.length);

        Object res = null;
        int i = 0;
        while (i < objs.length) {
            if (value.equals(objs[i])) {
                res = objs[i + 1];
                break;
            }
            if (i == objs.length - 1) {
                res = objs[i];
                break;
            }
            i += 2;
        }
        return res;
    }

    /**
     * 获取一个对象或数组的值
     *
     * @param value 对象
     * @param key   键名或索引
     * @return 值
     * @throws InvocationTargetException 如果调用目标javaBean错误
     * @throws IllegalAccessException    如果javaBean此字段不可访问
     */
    public static Object getValue(Object value, String key) throws InvocationTargetException, IllegalAccessException {
        if (value == null) {
            return null;
        }
        if (ReflectUtil.isBasicType(value)) {
            return null;
        }
        if (key.matches("\\d")) {
            int idx = Integer.parseInt(key);
            if (value instanceof Collection) {
                int i = 0;
                for (Object v : (Collection<?>) value) {
                    if (i++ == idx) {
                        return v;
                    }
                }
            }
            if (value instanceof Object[]) {
                return ((Object[]) value)[idx];
            }
        }
        if (value instanceof Map) {
            //noinspection unchecked
            Map<String, ?> map = (Map<String, ?>) value;
            if (map.containsKey(key)) {
                return map.get(key);
            }
            if (CollectionUtil.containsKeyIgnoreCase(map, key)) {
                return CollectionUtil.getValueIgnoreCase(map, key);
            }
        }
        Class<?> clazz = value.getClass();
        Method m = ReflectUtil.getGetMethod(clazz, key);
        if (m == null) {
            return null;
        }
        if (!m.isAccessible()) {
            m.setAccessible(true);
        }
        return m.invoke(value);
    }

    /**
     * 获取一个深层嵌套对象的值
     *
     * @param obj         深层嵌套的对象
     * @param jsonPathExp json路径表达式（{@code /a/b/0/name}）
     * @return 值
     * @throws InvocationTargetException 如果调用目标javaBean错误
     * @throws IllegalAccessException    如果javaBean此字段不可访问
     */
    public static Object getDeepNestValue(Object obj, String jsonPathExp) throws InvocationTargetException, IllegalAccessException {
        if (obj == null) {
            return null;
        }
        if (!jsonPathExp.startsWith("/")) {
            throw new IllegalArgumentException("json path expression syntax error, must startsWith '/', for example '/" + jsonPathExp + "'");
        }
        String trimStart = jsonPathExp.substring(1);
        if (!trimStart.contains("/")) {
            return getValue(obj, trimStart);
        }
        String key = trimStart.substring(0, trimStart.indexOf("/"));
        String tail = trimStart.substring(trimStart.indexOf("/"));
        return getDeepNestValue(getValue(obj, key), tail);
    }

    /**
     * 根据键名或路径从map中获取一个值
     *
     * @param args       map数据
     * @param nameOrPath 键名（可不区分大小写）或对象路径（e.g. user.name 区分大小写）
     * @return 值
     */
    public static Object getValueWild(Map<String, ?> args, String nameOrPath) {
        if (args == null) {
            return null;
        }
        Object v = null;
        if (args.containsKey(nameOrPath)) {
            v = args.get(nameOrPath);
        } else if (CollectionUtil.containsKeyIgnoreCase(args, nameOrPath)) {
            v = CollectionUtil.getValueIgnoreCase(args, nameOrPath);
        } else if (nameOrPath.contains(".")) {
            try {
                v = ObjectUtil.getDeepNestValue(args, "/" + nameOrPath.replace(".", "/"));
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return v;
    }

    /**
     * 从一个数组中返回第一个不为null的值，如果数组长度为0返回null
     *
     * @param values 一组值
     * @param <T>    类型参数
     * @return 不为null的值或全部为null
     */
    @SafeVarargs
    public static <T> T findFirstNonNull(T... values) {
        if (values.length == 0) {
            return null;
        }
        T res = null;
        for (T v : values) {
            if (v != null) {
                res = v;
                break;
            }
        }
        return res;
    }

    /**
     * 非空赋值表达式
     *
     * @param value 值
     * @param other 默认值
     * @param <T>   类型参数
     * @return 如果 value 为null 返回 other 否则返回 value
     */
    public static <T> T nullable(T value, T other) {
        return Optional.ofNullable(value).orElse(other);
    }
}
