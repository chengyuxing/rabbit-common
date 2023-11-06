package com.github.chengyuxing.common.script;

import java.util.Map;

/**
 * Abstract expression parser interface.
 */
public abstract class IExpression {

    protected final String expression;

    protected IExpression(String expression) {
        this.expression = expression;
    }

    /**
     * Calc expression.
     *
     * @param args args
     * @return true or false
     * @throws ArithmeticException if expression syntax error.
     */
    public abstract boolean calc(Map<String, ?> args);

    /**
     * Get value passed by pipes.
     *
     * @param value value
     * @param pipes pipes e.g.  <code>| {@link IPipe upper} | {@link IPipe length} | ...</code>
     * @return passed value
     */
    public Object pipedValue(Object value, String pipes) {
        return value;
    }

    @Override
    public String toString() {
        return expression;
    }
}
