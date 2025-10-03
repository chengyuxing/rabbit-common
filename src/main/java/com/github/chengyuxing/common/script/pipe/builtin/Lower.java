package com.github.chengyuxing.common.script.pipe.builtin;

import com.github.chengyuxing.common.script.pipe.IPipe;

/**
 * Make string lowercase.
 */
public class Lower implements IPipe<String> {
    @Override
    public String transform(Object value, Object... params) {
        return value.toString().toLowerCase();
    }
}