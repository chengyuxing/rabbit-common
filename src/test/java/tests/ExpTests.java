package tests;

import org.junit.Test;
import rabbit.common.types.FastExpression;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.regex.Matcher;

public class ExpTests {
    boolean exp = (true || false) && !(!(true && false || !!false)) || false;
    static String expression = "(true || false) && (!(true && false || !!false)) || false";
    static String expression2 = "true && false || !false";
    static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("nashorn");

    @Test
    public void test1() throws Exception {
        System.out.println(FastExpression.boolExpressionEval(expression));
    }

    @Test
    public void test2() throws Exception {
        System.out.println(SCRIPT_ENGINE.eval(expression));
    }
}
