package com.github.chengyuxing.common.script.ast.impl;

import java.util.List;

public class VarExpr extends ValueExpr {
    private final List<String> keys;

    public VarExpr(List<String> keys) {
        this.keys = keys;
    }

    public List<String> getKeys() {
        return keys;
    }

    @Override
    public Object eval(EvalContext context) {
        Object value = context.resolveArg(keys);
        return getPipedValue(value, context);
    }
}
