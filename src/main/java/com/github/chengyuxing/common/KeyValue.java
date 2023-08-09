package com.github.chengyuxing.common;

/**
 * key value 结构对象
 */
public final class KeyValue {
    private final String key;
    private final Object value;

    public KeyValue(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
