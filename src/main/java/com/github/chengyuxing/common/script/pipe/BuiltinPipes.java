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
    private static final Map<String, IPipe<?>> BUILTIN_PIPES;

    static {
        Map<String, IPipe<?>> pipes = new HashMap<>();
        pipes.put("length", new Length());
        pipes.put("upper", new Upper());
        pipes.put("lower", new Lower());
        pipes.put("kv", new Kv());
        pipes.put("type", new Type());
        pipes.put("split", new Split());
        pipes.put("nvl", new Nvl());
        pipes.put("in", new In());
        BUILTIN_PIPES = Collections.unmodifiableMap(pipes);
    }

    public static @NotNull @Unmodifiable Map<String, IPipe<?>> getAll() {
        return BUILTIN_PIPES;
    }
}
