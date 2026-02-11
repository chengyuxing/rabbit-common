package com.github.chengyuxing.common.script.ast;

import com.github.chengyuxing.common.script.ast.impl.EvalContext;
import com.github.chengyuxing.common.script.ast.impl.EvalResult;

import java.util.Map;

public interface ScriptEngine {
    ScriptAst compile(String script);

    EvalResult execute(ScriptAst ast, EvalContext context);

    default EvalResult run(String script, EvalContext context) {
        ScriptAst ast = compile(script);
        return execute(ast, context);
    }

    default EvalResult run(String script, Map<String, Object> args) {
        return run(script, new EvalContext(args));
    }
}
