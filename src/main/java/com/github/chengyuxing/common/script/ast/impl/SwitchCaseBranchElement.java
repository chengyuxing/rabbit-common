package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;

import java.util.List;

public class SwitchCaseBranchElement extends BranchElement {
    private final List<ValueExpr> valueExpr;

    public SwitchCaseBranchElement(List<ValueExpr> valueExpr, List<IElement> thenBlock) {
        this.valueExpr = valueExpr;
        setThenBlock(thenBlock);
    }

    public List<ValueExpr> getValueExpr() {
        return valueExpr;
    }
}
