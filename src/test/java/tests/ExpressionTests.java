package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.parser.RabbitScriptParser;
import org.intellij.lang.annotations.Subst;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionTests {
    static String exp = "!(:id >= 0 || :name <> blank) && :age<=21";
    static Map<String, Object> args = new HashMap<>();

    @BeforeClass
    public static void init() {
        args.put("id", -1);
        args.put("NAME", null);
        args.put("age", 17);
    }

    @Test
    public void fast() throws Exception {
        RabbitScriptParser parser = new RabbitScriptParser("#if !(:id >= 0 || :name <> blank) && :age<=21");
        boolean res = parser.evaluateCondition(args);
        boolean res1 = parser.evaluateCondition(args);
        System.out.println(res);
        System.out.println(res1);
    }

    @Test
    public void testD() {
        DataRow row = DataRow.of("user", DataRow.of("address", Arrays.asList("a", "b", "c")));
        String a = row.<String>getOptional("user.address.0").orElse("");
        System.out.println(a);
    }
}
