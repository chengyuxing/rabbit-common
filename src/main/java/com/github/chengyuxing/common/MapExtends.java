package com.github.chengyuxing.common;

import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * 对map接口进行一些扩张方法
 *
 * @param <V> 值类型参数
 */
public interface MapExtends<V> extends Map<String, V> {
    /**
     * 是否忽略大小写包含指定的key
     *
     * @param key 需要查找的key
     * @return 是否包含
     */
    default boolean containsKeyIgnoreCase(String key) {
        return ObjectUtil.containsKeyIgnoreCase(this, key);
    }

    /**
     * 忽略大小写获取一个值
     *
     * @param key 需要取值的key
     * @return 值
     */
    default V getIgnoreCase(String key) {
        return ObjectUtil.getValueIgnoreCase(this, key);
    }

    /**
     * 移除值为null的所有元素
     */
    default void removeIfAbsent() {
        Iterator<Entry<String, V>> iterator = entrySet().iterator();
        //为了内部一点性能就不使用函数接口了
        //noinspection Java8CollectionRemoveIf
        while (iterator.hasNext()) {
            if (iterator.next().getValue() == null) {
                iterator.remove();
            }
        }
    }

    /**
     * 移除值为null且不包含在指定keys中的所有元素
     *
     * @param keys 需忽略的键名集合
     */
    default void removeIfAbsentExclude(String... keys) {
        Iterator<Entry<String, V>> iterator = entrySet().iterator();
        //为了内部一点性能就不使用函数接口了
        //noinspection Java8CollectionRemoveIf
        while (iterator.hasNext()) {
            Entry<String, V> e = iterator.next();
            if (e.getValue() == null && !Arrays.asList(keys).contains(e.getKey())) {
                iterator.remove();
            }
        }
    }

    /**
     * 根据条件移除元素
     *
     * @param predicate 条件
     */
    default void removeIf(BiPredicate<String, V> predicate) {
        Iterator<Map.Entry<String, V>> iterator = entrySet().iterator();
        //为了内部一点性能就不使用函数接口了
        //noinspection Java8CollectionRemoveIf
        while (iterator.hasNext()) {
            Map.Entry<String, V> next = iterator.next();
            if (predicate.test(next.getKey(), next.getValue())) {
                iterator.remove();
            }
        }
    }
}
