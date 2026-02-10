package com.github.chengyuxing.common.script.ast;

import java.util.List;

public class ScriptAst {
    private final List<IElement> elements;

    public ScriptAst(List<IElement> elements) {
        this.elements = elements;
    }

    public List<IElement> getElements() {
        return elements;
    }
}
