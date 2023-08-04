package com.github.chengyuxing.common.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 集合工具类
 */
public final class CollectionUtil {
    /**
     * map是否忽略大小写包含指定的key
     *
     * @param map map
     * @param key 需要查找的key
     * @param <V> map值类型参数
     * @return 是否包含
     */
    public static <V> boolean containsKeyIgnoreCase(Map<String, V> map, String key) {
        Iterator<Map.Entry<String, V>> i = map.entrySet().iterator();
        if (key == null) {
            while (i.hasNext()) {
                Map.Entry<String, V> e = i.next();
                if (e.getKey() == null)
                    return true;
            }
        } else {
            while (i.hasNext()) {
                Map.Entry<String, V> e = i.next();
                if (key.equalsIgnoreCase(e.getKey()))
                    return true;
            }
        }
        return false;
    }

    /**
     * 集合中是否忽略大小写包含某个元素
     *
     * @param collection 集合
     * @param value      查询值
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(Collection<String> collection, String value) {
        Iterator<String> i = collection.iterator();
        if (value == null) {
            while (i.hasNext()) {
                if (i.next() == null) {
                    return true;
                }
            }
        } else {
            while (i.hasNext()) {
                if (i.next().equalsIgnoreCase(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * map的key忽略大小写获取一个值
     *
     * @param map map
     * @param key 需要取值的key
     * @param <V> 值类型参数
     * @return 值
     */
    public static <V> V getValueIgnoreCase(Map<String, V> map, String key) {
        Iterator<Map.Entry<String, V>> i = map.entrySet().iterator();
        if (key == null) {
            while (i.hasNext()) {
                Map.Entry<String, V> e = i.next();
                if (e.getKey() == null)
                    return e.getValue();
            }
        } else {
            while (i.hasNext()) {
                Map.Entry<String, V> e = i.next();
                if (key.equalsIgnoreCase(e.getKey()))
                    return e.getValue();
            }
        }
        return null;
    }

    public static boolean hasSameKeyIgnoreCase(Map<String, ?> map) {
        Set<String> keys = map.keySet();
        Set<String> newKeys = keys.stream().map(k -> {
            if (k == null) {
                return null;
            }
            return k.toLowerCase();
        }).collect(Collectors.toSet());
        return keys.size() != newKeys.size();
    }
}
