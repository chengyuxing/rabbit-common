package com.github.chengyuxing.common.script.pipe.builtin;

import com.github.chengyuxing.common.script.pipe.IPipe;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Returns the Array's size and Collection's size and string's length.
 */
public class Length implements IPipe<Integer> {

    @Override
    public Integer transform(Object value, Object... params) {
        if (value == null) {
            return 0;
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value);
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).size();
        }
        return value.toString().length();
    }
}