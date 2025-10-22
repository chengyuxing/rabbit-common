package com.github.chengyuxing.common.script.pipe.builtin;

import com.github.chengyuxing.common.script.pipe.IPipe;

import java.util.Arrays;

/**
 * Value is in the array or not.
 */
public class In implements IPipe<Boolean> {
    @Override
    public Boolean transform(Object value, Object... params) {
        return Arrays.asList(params).contains(value);
    }
}
