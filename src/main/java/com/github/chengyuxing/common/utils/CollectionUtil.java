package com.github.chengyuxing.common.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class CollectionUtil {

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
