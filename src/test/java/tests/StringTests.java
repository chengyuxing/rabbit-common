package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.DateTimes;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StringTests {
    @Test
    public void test() throws Exception {
        String sql = "${a}insert into ${Table} ${tables.fields} values (${VALUES.0.f}), (${values.0}), (${Values})${b}";
        System.out.println(StringUtil.replaceIgnoreCase(sql, "${values}", "cyx"));
        System.out.println(StringUtil.replaceIgnoreCase(sql, "${table}", "test.user"));
        System.out.println(StringUtil.replaceIgnoreCase(sql, "${A}", "?"));
        System.out.println(StringUtil.replaceIgnoreCase(sql, "${b}", "?"));
        System.out.println(StringUtil.replaceIgnoreCase(sql, "${b}", "${a}"));


        Map<String, Object> args = new HashMap<>();
        args.put("Table", "test.user");
        args.put("values", Arrays.asList("a", "b", "c"));
        args.put("VALUES", Arrays.asList(DataRow.fromPair("f", "c,d,f"), DataRow.fromPair("f", "x,v,z")));
        args.put("a", LocalDateTime.now());
        args.put("tables", DataRow.fromPair("fields", "id,name,age"));

        System.out.println(StringUtil.format(sql, args, o -> {
            if (o instanceof LocalDateTime) {
                return DateTimes.of((LocalDateTime) o).toString("yyyy/MM/dd");
            }
            return o.toString();
        }));
    }
}
