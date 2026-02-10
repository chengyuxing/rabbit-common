package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IExpr;

import java.util.Objects;

public class LogicExpr implements IExpr<Boolean> {
    private final IExpr<Boolean> left;
    private final String op;
    private final IExpr<Boolean> right;

    public LogicExpr(IExpr<Boolean> left, String op, IExpr<Boolean> right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public String getOp() {
        return op;
    }

    public IExpr<Boolean> getLeft() {
        return left;
    }

    public IExpr<Boolean> getRight() {
        return right;
    }

    @Override
    public Boolean eval(EvalContext context) {
        return Objects.equals(getOp(), "&&")
                ? getLeft().eval(context) && getRight().eval(context)
                : getLeft().eval(context) || getRight().eval(context);
    }
}
