package com.github.chengyuxing.common;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Map interface extends support some useful method.
 *
 * @param <V> value type
 */
public interface MapExtends<V> extends Map<String, V> {
    /**
     * Remove elements which value is null.
     */
    default void removeIfAbsent() {
        entrySet().removeIf(stringVEntry -> stringVEntry.getValue() == null);
    }

    /**
     * Remove elements which value is null but excludes special keys.
     *
     * @param keys excluded keys
     */
    default void removeIfAbsentExclude(String... keys) {
        entrySet().removeIf(e -> e.getValue() == null && !Arrays.asList(keys).contains(e.getKey()));
    }

    /**
     * Remove element by predicate.
     *
     * @param predicate predicate
     */
    default void removeIf(BiPredicate<String, V> predicate) {
        entrySet().removeIf(next -> predicate.test(next.getKey(), next.getValue()));
    }
}
