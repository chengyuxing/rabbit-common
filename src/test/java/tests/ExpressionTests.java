package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.parser.RabbitScriptParser;
import com.github.chengyuxing.common.util.ValueUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.entity.User;

import java.util.*;

public class ExpressionTests {
    static String exp = "!(:id >= 0 || :name <> blank) && :age<=21";
    static DataRow args = DataRow.of();

    @BeforeClass
    public static void init() {
        args.put("id", -1);
        args.put("NAME", null);
        args.put("age", 17);
        args.put("name", "cyx");
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
        User u = new User();
        u.setName("cyx");
        DataRow row = DataRow.of("user", DataRow.of(
                "address", new ArrayList<>(Arrays.asList("a", "b", "c")),
                "info", u
        ));
//        Set<String> sets = row.deepGetAs("user.address", v -> {
//            if (v instanceof List) {
//                return new HashSet<>((Collection<String>) v);
//            }
//            return Collections.emptySet();
//        });
        System.out.println(row.<Object>deepGetAs("user.address.1"));
//        System.out.println(sets);
//        System.out.println(sets.getClass());
    }

    @Test
    public void testF() {
        int[] ints = new int[]{1, 2, 3};
        List<Integer> list = ValueUtils.adaptValue(List.class, ints);
        System.out.println(list);
    }
}
