package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;
import com.github.chengyuxing.common.script.lang.ForContextProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForLoopElement implements IElement {
    private final String itemName;
    private final Map<ForContextProperty, String> contextProperties = new HashMap<>();
    private final ValueExpr valueExpr;
    private List<IElement> loopBlock;

    public ForLoopElement(String itemName, ValueExpr valueExpr) {
        this.itemName = itemName;
        this.valueExpr = valueExpr;
    }

    public String getItemName() {
        return itemName;
    }

    public ValueExpr getValueExpr() {
        return valueExpr;
    }

    public String getContextPropertyAlias(ForContextProperty contextProperty) {
        return contextProperties.get(contextProperty);
    }

    public List<IElement> getLoopBlock() {
        return loopBlock != null ? loopBlock : Collections.emptyList();
    }

    void setContextPropertyAlias(ForContextProperty contextProperty, String alias) {
        contextProperties.put(contextProperty, alias);
    }

    void setLoopBlock(List<IElement> loopBlock) {
        this.loopBlock = loopBlock;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return visitor.visitForLoop(this);
    }
}
