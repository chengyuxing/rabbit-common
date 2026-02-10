package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;

import java.util.List;

public class ChooseElement implements IElement {
    private final List<ChooseWhenBranchElement> whenBranches;
    private final BranchElement defaultBranch;

    public ChooseElement(List<ChooseWhenBranchElement> whenBranches, BranchElement defaultBranch) {
        this.whenBranches = whenBranches;
        this.defaultBranch = defaultBranch;
    }

    public List<ChooseWhenBranchElement> getWhenBranches() {
        return whenBranches;
    }

    public BranchElement getDefaultBranch() {
        return defaultBranch;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return visitor.visitChoose(this);
    }
}
