package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.KeyValue;
import com.github.chengyuxing.common.utils.ReflectUtil;

import java.util.*;

/**
 * 值转换管道基础接口
 *
 * @param <T> 目标类型
 */
@FunctionalInterface
public interface IPipe<T> {
    T transform(Object value);

    /**
     * 获取为字符串长度
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
     * 转为大写
     */
    class Upper implements IPipe<String> {

        @Override
        public String transform(Object value) {
            return value.toString().toUpperCase();
        }
    }

    /**
     * 转为小写
     */
    class Lower implements IPipe<String> {
        @Override
        public String transform(Object value) {
            return value.toString().toLowerCase();
        }
    }

    /**
     * Map转为元组集合 {@code [(item1 -> key, item2 -> value), ...]}
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
     * 对象转为Kv结构List
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
