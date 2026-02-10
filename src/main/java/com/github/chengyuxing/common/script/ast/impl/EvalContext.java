package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.exception.EvalRuntimeException;
import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.common.util.ValueUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EvalContext {
    private final Map<String, Object> inputArgs;
    private final ArrayDeque<Map<String, VarMeta>> scopedVars = new ArrayDeque<>();

    public EvalContext(@NotNull Map<String, Object> args) {
        this.inputArgs = new HashMap<>(args);
        this.scopedVars.push(new HashMap<>());
    }

    protected @NotNull Map<String, IPipe<?>> getPipes() {
        return Collections.emptyMap();
    }

    /**
     * Format the current scope plain text.
     * <p>
     * If the formatted result is not final form e.g. {@code :name} -> {@code :name_0}, persist the scope arguments to use for the next step.
     *
     * @param text   scope text
     * @param inputs input arguments
     * @param scope  current scope arguments
     * @return the formatted text and the scope arguments which used in the formatting
     */
    protected Pair<String, Map<String, Object>> formatScopePlainText(String text, Map<String, Object> inputs, Map<String, VarMeta> scope) {
        text = StringUtils.FMT.format(text, scope);
        text = StringUtils.FMT.format(text, inputs);
        return Pair.of(text, Collections.emptyMap());
    }

    Pair<String, Map<String, Object>> formatScopePlainText(String text) {
        Map<String, VarMeta> scope = scopedVars.peek();
        if (scope == null) {
            scope = Collections.emptyMap();
        }
        return formatScopePlainText(text,
                Collections.unmodifiableMap(inputArgs),
                Collections.unmodifiableMap(scope));
    }

    void pushScope() {
        if (scopedVars.isEmpty()) {
            scopedVars.push(new HashMap<>());
            return;
        }
        Map<String, VarMeta> current = new HashMap<>(scopedVars.peek());
        scopedVars.push(current);
    }

    void popScope() {
        scopedVars.pop();
    }

    void bindScope(String name, VarMeta value) {
        Map<String, VarMeta> scope = scopedVars.peek();
        if (scope == null) {
            throw new EvalRuntimeException("No active scope.");
        }
        if (scope.containsKey(name)) {
            throw new EvalRuntimeException("Variable '" + name + "' is already defined in the current scope.");
        }
        if (inputArgs.containsKey(name)) {
            throw new EvalRuntimeException("Variable '" + name + "' conflicts with input arguments.");
        }
        scope.put(name, value);
    }

    Object resolveArg(List<String> keys) {
        Map<String, VarMeta> scope = scopedVars.peek();
        if (scope != null) {
            String firstKey = keys.get(0);
            if (scope.containsKey(firstKey)) {
                if (keys.size() == 1) {
                    return scope.get(firstKey).getValue();
                }
                Map<String, Object> single = new HashMap<>(1);
                single.put(firstKey, scope.get(firstKey).getValue());
                return ValueUtils.accessDeepValue(single, keys);
            }
        }
        return ValueUtils.accessDeepValue(inputArgs, keys);
    }
}
