package com.github.chengyuxing.common.script.pipe.builtin;

import com.github.chengyuxing.common.script.pipe.IPipe;

/**
 * Make string uppercase.
 */
public class Upper implements IPipe<String> {

    @Override
    public String transform(Object value, Object... params) {
        return value.toString().toUpperCase();
    }
}