package com.github.chengyuxing.common;

/**
 * Named thread local object.
 *
 * @param <T> element type
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {
    private final String name;

    /**
     * Constructed a new NamedThreadLocal with name.
     *
     * @param name name
     */
    public NamedThreadLocal(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
