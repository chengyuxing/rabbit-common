package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;

public class VarDefineElement implements IElement {
    private final String name;
    private final ValueExpr valueExpr;

    public VarDefineElement(String name, ValueExpr valueExpr) {
        this.name = name;
        this.valueExpr = valueExpr;
    }

    public String getName() {
        return name;
    }

    public ValueExpr getValueExpr() {
        return valueExpr;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return visitor.visitVarDefine(this);
    }
}
