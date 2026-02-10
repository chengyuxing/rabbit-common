package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IExpr;
import com.github.chengyuxing.common.script.lang.Comparators;

public class BinaryExpr implements IExpr<Boolean> {
    private final ValueExpr left;
    private final String op;
    private final ValueExpr right;

    public BinaryExpr(ValueExpr left, String op, ValueExpr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public String getOp() {
        return op;
    }

    public ValueExpr getLeft() {
        return left;
    }

    public ValueExpr getRight() {
        return right;
    }

    @Override
    public Boolean eval(EvalContext context) {
        Object leftVal = getLeft().eval(context);
        Object rightVal = getRight().eval(context);
        return Comparators.compare(leftVal, getOp(), rightVal);
    }
}
