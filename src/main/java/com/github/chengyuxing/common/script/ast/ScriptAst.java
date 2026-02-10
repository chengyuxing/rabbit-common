package com.github.chengyuxing.common.script.ast;

import java.util.List;

public class ScriptAst {
    private final String source;
    private final List<IElement> elements;
    private final boolean dynamic;

    public ScriptAst(String source, List<IElement> elements, boolean dynamic) {
        this.source = source;
        this.elements = elements;
        this.dynamic = dynamic;
    }

    public String getSource() {
        return source;
    }

    public List<IElement> getElements() {
        return elements;
    }

    public boolean isDynamic() {
        return dynamic;
    }
}
