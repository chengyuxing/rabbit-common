package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IExpr;

public class NotExpr implements IExpr<Boolean> {
    private final IExpr<Boolean> expr;

    public NotExpr(IExpr<Boolean> expr) {
        this.expr = expr;
    }

    public IExpr<Boolean> getExpr() {
        return expr;
    }

    @Override
    public Boolean eval(EvalContext context) {
        return !getExpr().eval(context);
    }
}
