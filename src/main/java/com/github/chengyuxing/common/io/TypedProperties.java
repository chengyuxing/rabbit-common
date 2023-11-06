package com.github.chengyuxing.common.io;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Typed properties.
 */
public class TypedProperties extends Properties {
    /**
     * Parse to int and get.
     *
     * @param key key
     * @return int value
     */
    public int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }

    /**
     * Parse to int and get.
     *
     * @param key          key
     * @param defaultValue default value
     * @return int value
     */
    public int getInt(String key, int defaultValue) {
        if (containsKey(key)) {
            return getInt(key);
        }
        return defaultValue;
    }

    /**
     * Parse to boolean and get.
     *
     * @param key key
     * @return boolean value
     */
    public boolean getBool(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    /**
     * Parse to boolean and get.
     *
     * @param key          key
     * @param defaultValue default value
     * @return boolean value
     */
    public boolean getBool(String key, boolean defaultValue) {
        if (containsKey(key)) {
            return getBool(key);
        }
        return defaultValue;
    }

    /**
     * Parse to long and get.
     *
     * @param key key
     * @return long value
     */
    public long getLong(String key) {
        return Long.parseLong(getProperty(key));
    }

    /**
     * Parse to long and get.
     *
     * @param key          key
     * @param defaultValue default value
     * @return long value
     */
    public long getLong(String key, long defaultValue) {
        if (containsKey(key)) {
            return Long.parseLong(getProperty(key));
        }
        return defaultValue;
    }

    /**
     * Parse to double and get.
     *
     * @param key key
     * @return double value
     */
    public double getDouble(String key) {
        return Double.parseDouble(getProperty(key));
    }

    /**
     * Parse to double and get.
     *
     * @param key          key
     * @param defaultValue default value
     * @return double value
     */
    public double getDouble(String key, double defaultValue) {
        if (containsKey(key)) {
            return Double.parseDouble(getProperty(key));
        }
        return defaultValue;
    }

    /**
     * Parse to float and get.
     *
     * @param key key
     * @return float value
     */
    public float getFloat(String key) {
        return Float.parseFloat(getProperty(key));
    }

    /**
     * Parse to float and get.
     *
     * @param key          key
     * @param defaultValue default value
     * @return float value
     */
    public float getFloat(String key, float defaultValue) {
        if (containsKey(key)) {
            return Float.parseFloat(getProperty(key));
        }
        return defaultValue;
    }

    /**
     * Parse to stream by comma ({@code ,}) and get.
     *
     * @param key          key
     * @param defaultValue default value
     * @return Stream
     */
    public Stream<String> getStream(String key, Stream<String> defaultValue) {
        if (containsKey(key)) {
            return Stream.of(getProperty(key).split(","));
        }
        return defaultValue;
    }

    /**
     * Parse to list by comma ({@code ,}) and get.
     *
     * @param key          key
     * @param defaultValue default value
     * @return List
     */
    public List<String> getList(String key, List<String> defaultValue) {
        if (containsKey(key)) {
            return getStream(key, Stream.empty())
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());
        }
        return defaultValue;
    }

    /**
     * Parse to set by comma ({@code ,}) and get.
     *
     * @param key          key
     * @param defaultValue default value
     * @return Set
     */
    public Set<String> getSet(String key, Set<String> defaultValue) {
        List<String> list = getList(key, new ArrayList<>());
        if (!list.isEmpty()) {
            return new HashSet<>(list);
        }
        return defaultValue;
    }

    /**
     * Parse to map group by key prefix + dot ({@code .}) and get.
     *
     * @param key          key prefix e.g. datasource
     *                     <blockquote>
     *                     <ul>
     *                     <li>datasource.url=jdbc:... </li>
     *                     <li>datasource.username=...</li>
     *                     </ul>
     *                     </blockquote>
     * @param defaultValue default value
     * @return Map
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
