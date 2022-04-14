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
     * @param <T>    值类型
     * @param <R>    结果类型
     * @return 结果
     */
    @SuppressWarnings("unchecked")
    public static <T, R> R decode(T value, T equal, R result, Object... more) {
        Object[] objs = new Object[more.length + 2];
        objs[0] = equal;
        objs[1] = result;
        System.arraycopy(more, 0, objs, 2, more.length);
        boolean isOdd = (objs.length & 1) != 0;
        R res = null;
        for (int i = 0; i < objs.length; i += 2) {
            if (value.equals(objs[i])) {
                if (i < objs.length - 1)
                    res = (R) objs[i + 1];
                break;
            }
            if (isOdd && i == objs.length - 1) {
                res = (R) objs[i];
                break;
            }
        }
        return res;
    }

    /**
     * 获取一个对象或数组的值
     *
     * @param value 对象
     * @param key   键名或索引
     * @return 值
     * @throws NoSuchMethodException     如果是javaBean并且没有此字段的get方法
     * @throws InvocationTargetException 如果调用目标javaBean错误
     * @throws IllegalAccessException    如果javaBean此字段不可访问
     * @throws NoSuchFieldException      如果javaBean没有相应的字段
     */
    public static Object getValue(Object value, String key) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
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
            return ((Map<?, ?>) value).get(key);
        }
        Class<?> clazz = value.getClass();
        Class<?> type = clazz.getDeclaredField(key).getType();
        Method m = clazz.getDeclaredMethod(ReflectUtil.initGetMethod(key, type));
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
     * @throws NoSuchFieldException      如果是javaBean并且没有此字段
     * @throws InvocationTargetException 如果调用目标javaBean错误
     * @throws NoSuchMethodException     如果是javaBean并且没有此字段的get方法
     * @throws IllegalAccessException    如果javaBean此字段不可访问
     */
    public static Object getDeepNestValue(Object obj, String jsonPathExp) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
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
