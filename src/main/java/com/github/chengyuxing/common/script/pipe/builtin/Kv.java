package com.github.chengyuxing.common.script.pipe.builtin;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.KeyValue;
import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.common.utils.ReflectUtil;

import java.util.*;

/**
 * Convert Map or Java bean entity to key-value list: [(key, value), ...].
 */
public class Kv implements IPipe<List<KeyValue>> {
    @Override
    public List<KeyValue> transform(Object value, Object... params) {
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