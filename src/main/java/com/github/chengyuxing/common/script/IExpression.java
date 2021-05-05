package com.github.chengyuxing.common.script;

import java.util.Map;

/**
 * 抽象bool表达式通用接口
 */
public abstract class IExpression {

    protected final String expression;

    protected IExpression(String expression) {
        this.expression = expression;
    }

    public abstract boolean calc(Map<String, Object> args);
}
