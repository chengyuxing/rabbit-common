package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;
import com.github.chengyuxing.common.script.ast.IExpr;

import java.util.List;

public class GuardElement implements IElement {
    private final IExpr<Boolean> expr;
    private final List<IElement> thenBlock;
    private final String message;

    public GuardElement(IExpr<Boolean> expr, List<IElement> thenBlock, String message) {
        this.expr = expr;
        this.thenBlock = thenBlock;
        this.message = message;
    }

    public IExpr<Boolean> getExpr() {
        return expr;
    }

    public List<IElement> getThenBlock() {
        return thenBlock;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return  visitor.visitGuard(this);
    }
}
