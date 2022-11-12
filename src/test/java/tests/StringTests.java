package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StringTests {
    static String sql = "${   a   } ${a.d} insert into ${  Table  } ${tables.fields} values (${  VALUES.1.f }), (${values.0}), (${  Values   })${b}";

    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("Table", "test.user");
        args.put("values", Arrays.asList("a", "b", "c"));
        args.put("VALUES", Arrays.asList(DataRow.fromPair("f", "c,d,f"), DataRow.fromPair("f", "x,v,z")));
        args.put("a", LocalDateTime.now());
        args.put("a.d", "LocalDateTime.now()");
        args.put("tables", DataRow.fromPair("fields", "id,name,age"));

        System.out.println(StringUtil.format(sql, args));
    }

    @Test
    public void test6() throws Exception {
        String str = "${ user } <> blank && ${ user.name } !~ 'j'";
        System.out.println(StringUtil.format(str, DataRow.fromPair("user", ":user", "user.name", ":user.name")));
    }

    @Test
    public void test2() throws Exception {
        String str = "insert into ${ table } ${fields} id, name, age values (${ values.data.1 }), (${values.data.1})";
        Object values = DataRow.fromPair("data", Arrays.asList("1,2,3", "4,5,6"));

        String res = StringUtil.format(str, "table", "test.user");
        String res2 = StringUtil.format(str, "values", values);
        System.out.println(res);
        System.out.println(res2);
    }

    @Test
    public void test3() throws Exception {
        System.out.println(StringUtil.format("${a.b}", "a", "b"));
    }

    @Test
    public void testx1() throws Exception{
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE));
    }
}
