package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;

import java.util.Collections;
import java.util.List;

public class BranchElement implements IElement {
    private List<IElement> thenBlock;

    public BranchElement() {
    }

    public List<IElement> getThenBlock() {
        return thenBlock != null ? thenBlock : Collections.emptyList();
    }

    void setThenBlock(List<IElement> thenBlock) {
        this.thenBlock = thenBlock;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return null;
    }
}
