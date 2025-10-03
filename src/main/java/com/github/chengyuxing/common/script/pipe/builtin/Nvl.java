package com.github.chengyuxing.common.script.pipe.builtin;

import com.github.chengyuxing.common.script.pipe.IPipe;

/**
 * If the value is null, returns the first param.
 */
public class Nvl implements IPipe<Object> {
    @Override
    public Object transform(Object value, Object... params) {
        if (params.length != 1) {
            throw new IllegalArgumentException("nvl requires a single argument");
        }
        return value == null ? params[0] : value;
    }
}
