package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IExpr;
import com.github.chengyuxing.common.script.exception.PipeNotFoundException;
import com.github.chengyuxing.common.script.pipe.BuiltinPipes;
import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.common.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class ValueExpr implements IExpr<Object> {
    private static final Map<String, IPipe<?>> BUILTIN_PIPES = BuiltinPipes.getAll();
    private List<Pair<String, List<ValueExpr>>> pipes;

    public List<Pair<String, List<ValueExpr>>> getPipes() {
        return pipes != null ? pipes : Collections.emptyList();
    }

    protected Object getPipedValue(Object value, EvalContext context) {
        Object pipedValue = value;
        for (Pair<String, List<ValueExpr>> pipe : getPipes()) {
            Object[] myParams = new Object[pipe.getItem2().size()];
            for (int i = 0; i < pipe.getItem2().size(); i++) {
                ValueExpr valueExpr = pipe.getItem2().get(i);
                myParams[i] = valueExpr.eval(context);
            }

            String pipeName = pipe.getItem1();
            if (context.getPipes().containsKey(pipeName)) {
                pipedValue = context.getPipes().get(pipeName).transform(pipedValue, myParams);
            } else if (BUILTIN_PIPES.containsKey(pipeName)) {
                pipedValue = BUILTIN_PIPES.get(pipeName).transform(pipedValue, myParams);
            } else {
                throw new PipeNotFoundException("Cannot find pipe '" + pipe + "'");
            }
        }
        return pipedValue;
    }

    void setPipes(List<Pair<String, List<ValueExpr>>> pipes) {
        this.pipes = pipes;
    }
}
