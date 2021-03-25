package tests;

import org.junit.BeforeClass;
import org.junit.Test;
import rabbit.common.script.impl.CExpression;
import rabbit.common.script.impl.FastExpression;

import java.util.HashMap;
import java.util.Map;

public class ExpressionTests {
    static String exp = "!(:id >= 0 || :name <> blank) && :age<=21";
    static Map<String, Object> args = new HashMap<>();

    @BeforeClass
    public static void init() {
        args.put("id", -1);
        args.put("name", null);
        args.put("age", 17);
    }

    @Test
    public void fast() throws Exception {
        FastExpression expression = FastExpression.of(exp);
        System.out.println(expression.calc(args));
    }

    @Test
    public void c() throws Exception {
        CExpression expression = CExpression.of(exp);
        System.out.println(expression.calc(args));
    }

}
