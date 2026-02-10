package com.github.chengyuxing.common.script.ast.impl;

public class ConstExpr extends ValueExpr {
    private final Object value;

    public ConstExpr(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Object eval(EvalContext context) {
        return getPipedValue(value, context);
    }
}
