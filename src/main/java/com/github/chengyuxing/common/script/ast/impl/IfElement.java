package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElementVisitor;
import com.github.chengyuxing.common.script.ast.IExpr;
import com.github.chengyuxing.common.script.ast.IElement;

import java.util.List;

public class IfElement implements IElement {
    private final IExpr<Boolean> expr;
    private final List<IElement> thenBlock;
    private final List<IElement> elseBlock;

    public IfElement(IExpr<Boolean> expr, List<IElement> thenBlock, List<IElement> elseBlock) {
        this.expr = expr;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public IExpr<Boolean> getExpr() {
        return expr;
    }

    public List<IElement> getThenBlock() {
        return thenBlock;
    }

    public List<IElement> getElseBlock() {
        return elseBlock;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return visitor.visitIf(this);
    }
}
