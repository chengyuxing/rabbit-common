package com.github.chengyuxing.common;

/**
 * Key value structured object.
 */
public final class KeyValue {
    private final String key;
    private final Object value;

    /**
     * Constructs a new KeyValue with initial key and value.
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

    @Override
    public String toString() {
        return '{' +
                "key=" + key +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValue)) return false;

        KeyValue keyValue = (KeyValue) o;

        if (getKey() != null ? !getKey().equals(keyValue.getKey()) : keyValue.getKey() != null) return false;
        return getValue() != null ? getValue().equals(keyValue.getValue()) : keyValue.getValue() == null;
    }

    @Override
    public int hashCode() {
        int result = getKey() != null ? getKey().hashCode() : 0;
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        return result;
    }
}
