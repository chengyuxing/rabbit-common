package tests;

import com.github.chengyuxing.common.script.IExpression;

import java.util.Map;

public class XExpression extends IExpression {
    protected XExpression(String expression) {
        super(expression);
    }

    @Override
    public boolean calc(Map<String, ?> args) {
        return expression.startsWith("a");
    }
}
