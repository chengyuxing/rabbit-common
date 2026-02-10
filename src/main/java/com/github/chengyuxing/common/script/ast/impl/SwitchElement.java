package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;

import java.util.List;

public class SwitchElement implements IElement {
    private final ValueExpr valueExpr;
    private final List<SwitchCaseBranchElement> caseBranches;
    private final BranchElement defaultBranch;

    public SwitchElement(ValueExpr valueExpr, List<SwitchCaseBranchElement> caseBranches, BranchElement defaultBranch) {
        this.valueExpr = valueExpr;
        this.caseBranches = caseBranches;
        this.defaultBranch = defaultBranch;
    }

    public ValueExpr getValueExpr() {
        return valueExpr;
    }

    public List<SwitchCaseBranchElement> getCaseBranches() {
        return caseBranches;
    }

    public BranchElement getDefaultBranch() {
        return defaultBranch;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return  visitor.visitSwitch(this);
    }
}
