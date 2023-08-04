package com.github.chengyuxing.common;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * 对map接口进行一些扩张方法
 *
 * @param <V> 值类型参数
 */
public interface MapExtends<V> extends Map<String, V> {
    /**
     * 移除值为null的所有元素
     */
    default void removeIfAbsent() {
        entrySet().removeIf(stringVEntry -> stringVEntry.getValue() == null);
    }

    /**
     * 移除值为null且不包含在指定keys中的所有元素
     *
     * @param keys 需忽略的键名集合
     */
    default void removeIfAbsentExclude(String... keys) {
        entrySet().removeIf(e -> e.getValue() == null && !Arrays.asList(keys).contains(e.getKey()));
    }

    /**
     * 根据条件移除元素
     *
     * @param predicate 条件
     */
    default void removeIf(BiPredicate<String, V> predicate) {
        entrySet().removeIf(next -> predicate.test(next.getKey(), next.getValue()));
    }
}
