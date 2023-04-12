package com.github.chengyuxing.common.io;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 值类型化的properties
 */
public class TypedProperties extends Properties {
    /**
     * 获取一个整型
     *
     * @param key 键名
     * @return 整型数值
     */
    public int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }

    /**
     * 获取一个整型
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 整型数值
     */
    public int getInt(String key, int defaultValue) {
        if (containsKey(key)) {
            return getInt(key);
        }
        return defaultValue;
    }

    /**
     * 获取一个布尔值
     *
     * @param key 键名
     * @return 布尔数值
     */
    public boolean getBool(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    /**
     * 获取一个布尔值
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 布尔数值
     */
    public boolean getBool(String key, boolean defaultValue) {
        if (containsKey(key)) {
            return getBool(key);
        }
        return defaultValue;
    }

    /**
     * 获取一个长整型
     *
     * @param key 键名
     * @return 长整型数值
     */
    public long getLong(String key) {
        return Long.parseLong(getProperty(key));
    }

    /**
     * 获取一个长整型
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 长整型数值
     */
    public long getLong(String key, long defaultValue) {
        if (containsKey(key)) {
            return Long.parseLong(getProperty(key));
        }
        return defaultValue;
    }

    /**
     * 获取一个双精度型
     *
     * @param key 键名
     * @return 双精度数值
     */
    public double getDouble(String key) {
        return Double.parseDouble(getProperty(key));
    }

    /**
     * 获取一个双精度型
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 双精度数值
     */
    public double getDouble(String key, double defaultValue) {
        if (containsKey(key)) {
            return Double.parseDouble(getProperty(key));
        }
        return defaultValue;
    }

    /**
     * 获取一个浮点型
     *
     * @param key 键名
     * @return 浮点型数值
     */
    public float getFloat(String key) {
        return Float.parseFloat(getProperty(key));
    }

    /**
     * 获取一个浮点型
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 浮点型数值
     */
    public float getFloat(String key, float defaultValue) {
        if (containsKey(key)) {
            return Float.parseFloat(getProperty(key));
        }
        return defaultValue;
    }

    /**
     * 根据逗号({@code ,})分割获取一个Stream
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 一个Stream
     */
    public Stream<String> getStream(String key, Stream<String> defaultValue) {
        if (containsKey(key)) {
            return Stream.of(getProperty(key).split(","));
        }
        return defaultValue;
    }

    /**
     * 根据逗号({@code ,})分割获取一个List，不包含空元素
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 一个List
     */
    public List<String> getList(String key, List<String> defaultValue) {
        if (containsKey(key)) {
            return getStream(key, Stream.empty())
                    .map(String::trim)
                    .filter(item -> !item.equals(""))
                    .collect(Collectors.toList());
        }
        return defaultValue;
    }

    /**
     * 根据逗号({@code ,})分割获取一个Set，不包含空元素
     *
     * @param key          键名
     * @param defaultValue 默认值
     * @return 一个Set
     */
    public Set<String> getSet(String key, Set<String> defaultValue) {
        List<String> list = getList(key, new ArrayList<>());
        if (!list.isEmpty()) {
            return new HashSet<>(list);
        }
        return defaultValue;
    }

    /**
     * 根据键名前缀({@code .})获取一个Map
     *
     * @param key          键名前缀 e.g: datasource
     *                     <blockquote>
     *                     <ul>
     *                     <li>datasource.url=jdbc:... </li>
     *                     <li>datasource.username=...</li>
     *                     </ul>
     *                     </blockquote>
     * @param defaultValue 默认值
     * @return 一个Map
     */
    public Map<String, String> getMap(String key, Map<String, String> defaultValue) {
        Map<String, String> map = keySet().stream()
                .map(Object::toString)
                .map(String::trim)
                .filter(k -> k.startsWith(key + "."))
                .filter(k -> k.length() > key.length() + 1)
                .collect(Collectors.toMap(k -> k.substring(k.indexOf(".") + 1), this::getProperty));
        if (map.isEmpty()) {
            return defaultValue;
        }
        return map;
    }
}
