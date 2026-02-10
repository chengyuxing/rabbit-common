package com.github.chengyuxing.common.script.ast;

import com.github.chengyuxing.common.script.ast.impl.EvalContext;

public interface IExpr<R> {
    R eval(EvalContext context);
}
