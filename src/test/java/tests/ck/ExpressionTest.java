package tests.ck;

import com.github.chengyuxing.common.console.Color;
import com.github.chengyuxing.common.console.Printer;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;
import com.github.chengyuxing.common.script.impl.CExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        System.out.println(exp.calc(args, true));

    }

    @Test
    public void num() throws Exception {
        Printer.println("我喜欢你", Color.PURPLE);
        System.out.println(Printer.colorful("我喜欢你", Color.RED) + Printer.colorful("Hello world!", Color.SILVER));
    }
}
