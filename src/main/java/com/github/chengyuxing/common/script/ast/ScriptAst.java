package com.github.chengyuxing.common.script.ast;

import com.github.chengyuxing.common.script.ast.impl.TextElement;

import java.util.List;

public class ScriptAst {
    private final List<IElement> elements;
    private final boolean dynamic;

    public ScriptAst(List<IElement> elements) {
        this.elements = elements;
        this.dynamic = !(elements.isEmpty() || (elements.size() == 1 && elements.get(0) instanceof TextElement));
    }

    public List<IElement> getElements() {
        return elements;
    }

    public boolean isDynamic() {
        return dynamic;
    }
}
