package com.github.chengyuxing.common.script.ast;

public interface IElement {
    <R> R accept(IElementVisitor<R> visitor);
}
