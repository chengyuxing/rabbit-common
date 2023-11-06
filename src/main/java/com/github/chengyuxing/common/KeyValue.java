package com.github.chengyuxing.common;

/**
 * Key value structured object.
 */
public final class KeyValue {
    private final String key;
    private final Object value;

    /**
     * Constructed a KeyValue with initial key and value.
     *
     * @param key   key
     * @param value value
     */
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
