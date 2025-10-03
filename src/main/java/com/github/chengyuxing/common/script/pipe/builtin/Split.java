package com.github.chengyuxing.common.script.pipe.builtin;

import com.github.chengyuxing.common.script.pipe.IPipe;

/**
 * Split string to array by delimiter.
 */
public class Split implements IPipe<String[]> {
    @Override
    public String[] transform(Object value, Object... params) {
        if (params.length != 1) {
            throw new IllegalArgumentException("split params must have exactly one value");
        }
        if (value == null) {
            return new String[0];
        }
        return value.toString().split(params[0].toString());
    }
}