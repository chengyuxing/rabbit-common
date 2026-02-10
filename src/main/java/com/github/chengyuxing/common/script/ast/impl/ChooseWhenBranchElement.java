package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IExpr;

import java.util.List;

public class ChooseWhenBranchElement extends BranchElement {
    private final IExpr<Boolean> expr;

    public ChooseWhenBranchElement(IExpr<Boolean> expr, List<IElement> whenBlock) {
        this.expr = expr;
        setThenBlock(whenBlock);
    }

    public IExpr<Boolean> getExpr() {
        return expr;
    }

}
