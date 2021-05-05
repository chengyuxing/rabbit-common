package tests.ck;

import org.junit.Test;
import com.github.chengyuxing.common.script.impl.CExpression;

import java.util.HashMap;
import java.util.Map;

public class ExpressionTest {

    @Test
    public void e() throws Exception {
        String expression = "(:id == 1 && :name == 'json') || :id == 3";
        String expression2 = "!(   :id ==       -0.25 && :name == '我的') || (:id == 3 && :name == 'mike')";

        Map<String, Object> args = new HashMap<>();
        args.put("id", 3);
        args.put("name", "mike");

        CExpression exp = CExpression.of(expression2);
        System.out.println(exp.calc(args));

    }

    @Test
    public void num() throws Exception {
//        System.out.println("-0.09".matches(CExpression.NUMBER_REGEX));
    }
}
