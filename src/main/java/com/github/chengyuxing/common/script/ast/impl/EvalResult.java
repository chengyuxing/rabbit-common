package com.github.chengyuxing.common.script.ast.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;

public class EvalResult {
    private final String content;
    private final Map<String, Object> usedVars;

    public EvalResult(String content, Map<String, Object> usedVars) {
        this.content = content;
        this.usedVars = Collections.unmodifiableMap(usedVars);
    }

    public @NotNull String getContent() {
        return content;
    }

    public @NotNull @Unmodifiable Map<String, Object> getUsedVars() {
        return usedVars;
    }
}
