package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IElementVisitor;

import java.util.Collections;
import java.util.List;

public class ForLoopElement implements IElement {
    private final String itemName;
    private String indexName;
    private String firstName;
    private String lastName;
    private String oddName;
    private String evenName;
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

    public String getIndexName() {
        return indexName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEvenName() {
        return evenName;
    }

    public String getOddName() {
        return oddName;
    }

    public List<IElement> getLoopBlock() {
        return loopBlock != null ? loopBlock : Collections.emptyList();
    }

    void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    void setLastName(String lastName) {
        this.lastName = lastName;
    }

    void setEvenName(String evenName) {
        this.evenName = evenName;
    }

    void setOddName(String oddName) {
        this.oddName = oddName;
    }

    void setLoopBlock(List<IElement> loopBlock) {
        this.loopBlock = loopBlock;
    }

    @Override
    public <R> R accept(IElementVisitor<R> visitor) {
        return visitor.visitForLoop(this);
    }
}
