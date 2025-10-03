package com.github.chengyuxing.common.script.pipe;

import com.github.chengyuxing.common.script.pipe.builtin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Built-in pipes.
 */
public final class BuiltinPipes {
    private static final Map<String, IPipe<?>> BUILTIN_PIPES = new HashMap<>();

    static {
        BUILTIN_PIPES.put("length", new Length());
        BUILTIN_PIPES.put("upper", new Upper());
        BUILTIN_PIPES.put("lower", new Lower());
        BUILTIN_PIPES.put("kv", new Kv());
        BUILTIN_PIPES.put("type", new Type());
        BUILTIN_PIPES.put("split", new Split());
        BUILTIN_PIPES.put("nvl", new Nvl());
    }

    public static @NotNull @Unmodifiable Map<String, IPipe<?>> getAll() {
        return Collections.unmodifiableMap(BUILTIN_PIPES);
    }
}
