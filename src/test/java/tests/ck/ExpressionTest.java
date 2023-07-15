package tests.ck;

import com.github.chengyuxing.common.console.Color;
import com.github.chengyuxing.common.console.Printer;
import org.junit.Test;

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

    }

    @Test
    public void num() throws Exception {
        Printer.println("我喜欢你", Color.PURPLE);
        System.out.println(Printer.colorful(Printer.colorful("我喜欢你", Color.BLUE), Color.RED) + Printer.colorful("Hello world!", Color.SILVER));
        System.out.println(Printer.underline(Printer.colorful("abc", Color.RED)));
    }
}
