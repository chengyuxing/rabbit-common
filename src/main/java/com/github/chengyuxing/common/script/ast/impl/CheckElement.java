package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;
import com.github.chengyuxing.common.script.ast.IExpr;

public class CheckElement implements IElement {
    private final IExpr<Boolean> expr;
    private final String message;

    public CheckElement(IExpr<Boolean> expr, String message) {
        this.expr = expr;
        this.message = message;
    }

    public IExpr<Boolean> getExpr() {
        return expr;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return  visitor.visitCheck(this);
    }
}
