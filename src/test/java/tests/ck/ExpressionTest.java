package tests.ck;

import com.github.chengyuxing.common.console.Style;
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
        Printer.println("我喜欢你", Style.PURPLE);
        System.out.println(Printer.colorful("Hello world!", Style.RED, Style.UNDERLINE));
        System.out.println("\033[2;3;4maskfjfkf\033[0m".replaceAll("", ""));
    }
}
