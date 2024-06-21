package com.github.chengyuxing.common.script.expression;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.KeyValue;
import com.github.chengyuxing.common.utils.ReflectUtil;

import java.util.*;

/**
 * Pipe function interface.
 *
 * @param <T> result type
 */
@FunctionalInterface
public interface IPipe<T> {
    T transform(Object value);

    /**
     * String content length.
     */
    class Length implements IPipe<Integer> {

        @Override
        public Integer transform(Object value) {
            if (value == null) {
                return -1;
            }
            return value.toString().length();
        }
    }

    /**
     * To uppercase.
     */
    class Upper implements IPipe<String> {

        @Override
        public String transform(Object value) {
            return value.toString().toUpperCase();
        }
    }

    /**
     * To lowercase.
     */
    class Lower implements IPipe<String> {
        @Override
        public String transform(Object value) {
            return value.toString().toLowerCase();
        }
    }

    /**
     * Map to pair list: {@code [(item1 -> key, item2 -> value), ...]}.
     */
    class Map2Pairs implements IPipe<List<Pair<String, Object>>> {
        @Override
        public List<Pair<String, Object>> transform(Object value) {
            if (value instanceof Map) {
                List<Pair<String, Object>> list = new ArrayList<>(((Map<?, ?>) value).size());
                ((Map<?, ?>) value).forEach((k, v) -> list.add(Pair.of(k.toString(), v)));
                return list;
            }
            return Collections.emptyList();
        }
    }

    /**
     * Object to key-value list: [(key, value), ...].
     */
    class Kv implements IPipe<List<KeyValue>> {
        @Override
        public List<KeyValue> transform(Object value) {
            if (Objects.isNull(value) || ReflectUtil.isBasicType(value)) {
                return Collections.emptyList();
            }
            Map<?, ?> map = value instanceof Map ? (Map<?, ?>) value : DataRow.ofEntity(value);
            List<KeyValue> keyValues = new ArrayList<>(map.size());
            map.forEach((k, v) -> {
                if (Objects.nonNull(k)) {
                    keyValues.add(new KeyValue(k.toString(), v));
                }
            });
            return keyValues;
        }
    }

}
