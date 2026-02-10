package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IExpr;
import com.github.chengyuxing.common.script.lang.Comparators;

public class UnaryExpr implements IExpr<Boolean> {
    private final ValueExpr left;

    public UnaryExpr(ValueExpr left) {
        this.left = left;
    }

    public ValueExpr getLeft() {
        return left;
    }

    @Override
    public Boolean eval(EvalContext context) {
        Object value = left.eval(context);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return !Comparators.compare(value, "=", "");
    }
}
