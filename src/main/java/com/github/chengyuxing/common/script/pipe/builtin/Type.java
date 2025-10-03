package com.github.chengyuxing.common.script.pipe.builtin;

import com.github.chengyuxing.common.script.pipe.IPipe;

/**
 * Returns the value type name ({@link Class#getSimpleName()}).
 */
public class Type implements IPipe<String> {
    @Override
    public String transform(Object value, Object... params) {
        if (value == null) {
            return "";
        }
        return value.getClass().getSimpleName();
    }
}