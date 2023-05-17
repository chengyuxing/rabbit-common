package tests;

import com.github.chengyuxing.common.script.IExpression;
import com.github.chengyuxing.common.script.IPipe;

import java.util.Map;

public class XExpression extends IExpression {
    protected XExpression(String expression) {
        super(expression);
    }

    @Override
    public boolean calc(Map<String, ?> args,boolean require) {
        return expression.startsWith("a");
    }

    @Override
    public void setPipes(Map<String, IPipe<?>> pipes) {

    }
}
