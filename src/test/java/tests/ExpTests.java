package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.script.impl.FastExpression;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;
import tests.entity.Coord;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpTests {
    boolean exp = (true || false) && (!(true && false || !!false)) || false;
    boolean exp2 = true && false || !false;
    static String expression = "(true || false) && (!(true && false || !!false)) || false";
    static String expression2 = "true && false || !false";
    static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("nashorn");

    @Test
    public void regexTest() throws Exception {
        FastExpression expression = FastExpression.of(":id @ '^2\\d*$'");
        Map<String, Object> map = new HashMap<>();
        map.put("id", "2");
        boolean res = expression.calc(map);
        System.out.println(res);
    }

    @Test
    public void str() throws Exception {
        Pattern p = Pattern.compile("^'(\\S+)'$");
        Matcher m = p.matcher("'abc''def'''d'");
        while (m.find()) {
            System.out.println(m.group());
        }
    }

    @Test
    public void test1() throws Exception {
        System.out.println(FastExpression.boolExpressionEval(expression));
        System.out.println(FastExpression.boolExpressionEval(expression2));
    }

    @Test
    public void test3() throws Exception {
        System.out.println(FastExpression.boolExpressionEval("true && false && true && true && true"));
    }

    @Test
    public void test2() throws Exception {
        System.out.println(SCRIPT_ENGINE.eval(expression));
    }

    @Test
    public void ccc() throws Exception {
        System.out.println("string.class".substring(0, "string.class".lastIndexOf(".class")));
    }

    @Test
    public void starts() throws Exception {
        System.out.println(StringUtil.startsWithsIgnoreCase("aaaaaaa", new String[0]));
        System.out.println("aaaa".startsWith(""));
    }

    @Test
    public void eq() throws Exception {
        System.out.println(Comparators.equal(Collections.emptyList(), "blank"));
    }

    @Test
    public void pickTest() throws Exception {
        DataRow row = DataRow.fromPair("a", 1, "b", 2, "c", 3, "d", 4);
        System.out.println(row.pick("d", "c", "a"));
        System.out.println((Integer) row.at("/c"));
    }

    @Test
    public void testJson() throws Exception {
        DataRow row = DataRow.fromPair("user", Arrays.asList("a", "b", "c", new Coord(18, 22)));
        System.out.println(row);
        System.out.println((Integer) row.at("/user/3/y"));
        System.out.println((Integer) row.at(0, 3, "x"));
        System.out.println("------------------------");
        DataRow row2 = DataRow.fromMap(row.toMap());
        System.out.println(row2.get(new Integer(0)));
        System.out.println(row2.at("/user/3/x"));
        System.out.println(row2.at(0, 2));
    }

    @Test
    public void deepValue() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Coord coord = new Coord(11, 34);
        map.put("coord", coord);
        list.add(map);

        String props = "/0/coord/x";

        System.out.println(ObjectUtil.getDeepNestValue(list, props));
    }
}
