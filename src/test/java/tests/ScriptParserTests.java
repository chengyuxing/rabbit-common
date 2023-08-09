package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.IPipe;
import com.github.chengyuxing.common.script.SimpleScriptParser;
import com.github.chengyuxing.common.KeyValue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScriptParserTests {
    @Test
    public void testSqlParser() {
        SimpleScriptParser parser = new SimpleScriptParser();
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            String sql = parser.parse("select * from test.user where id = 1\n" +
                    " #for id of :ids delimiter ', \\n' open ' or id in (' close ')'\n" +
                    "     #if :id >= 2\n" +
                    "    :_for.id\n" +
                    "     #fi\n" +
                    " #done", DataRow.of("ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 1 + i, 11, 23, 45, 55, 67)));
            data.add(parser.getForVars());
            if (i == 9999) {
                System.out.println(sql);
            }
        }
        System.out.println(data.size());
    }

    @Test
    public void test1() {
        IPipe.Kv kv = new IPipe.Kv();
        User user = new User();
        user.setName("cyx");
        user.setAge(27);
        user.setAddress("kunming");
        List<KeyValue> keyValues = kv.transform(user);
        System.out.println(keyValues);
    }
}
