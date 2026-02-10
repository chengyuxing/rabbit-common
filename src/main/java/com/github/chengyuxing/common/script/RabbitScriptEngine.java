package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.ScriptAst;
import com.github.chengyuxing.common.script.ast.ScriptEngine;
import com.github.chengyuxing.common.script.ast.impl.*;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;

import java.util.List;

public final class RabbitScriptEngine implements ScriptEngine {
    @Override
    public ScriptAst compile(String script) {
        RabbitScriptLexer lexer = new RabbitScriptLexer(script);
        RabbitScriptParser parser = new RabbitScriptParser(lexer.tokenize());
        List<IElement> elements = parser.parse();
        int size = elements.size();
        boolean dynamic = !(size == 0 || (size == 1 && elements.get(0) instanceof TextElement));
        return new ScriptAst(script, elements, dynamic);
    }

    @Override
    public EvalResult execute(ScriptAst ast, EvalContext context) {
        RabbitScriptEvaluator evaluator = new RabbitScriptEvaluator(context);
        return evaluator.execute(ast);
    }
}
