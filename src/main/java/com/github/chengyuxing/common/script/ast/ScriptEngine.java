package com.github.chengyuxing.common.script.ast;

import com.github.chengyuxing.common.script.ast.impl.EvalContext;
import com.github.chengyuxing.common.script.ast.impl.EvalResult;

public interface ScriptEngine {
    ScriptAst compile(String script);

    EvalResult execute(ScriptAst ast, EvalContext context);
}
