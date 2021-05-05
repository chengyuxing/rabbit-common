package tests;

import org.junit.Test;
import com.github.chengyuxing.common.script.impl.FastExpression;
import com.github.chengyuxing.common.utils.StringUtil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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

    @Test
    public void ccc() throws Exception{
        System.out.println("string.class".substring(0,"string.class".lastIndexOf(".class")));
    }

    @Test
    public void starts() throws Exception{
        System.out.println(StringUtil.startsWithsIgnoreCase("aaaaaaa",new String[0]));
        System.out.println("aaaa".startsWith(""));
    }
}
