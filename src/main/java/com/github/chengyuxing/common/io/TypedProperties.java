package com.github.chengyuxing.common.io;

import java.util.Properties;

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
}
