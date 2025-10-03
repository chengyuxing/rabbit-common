package com.github.chengyuxing.common.script.pipe;

/**
 * Pipe function interface.
 *
 * @param <T> result type
 */
@FunctionalInterface
public interface IPipe<T> {
    /**
     * Do transfer value.
     *
     * @param value  value
     * @param params params
     * @return new value
     */
    T transform(Object value, Object... params);
}
