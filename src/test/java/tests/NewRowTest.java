package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.utils.ReflectUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NewRowTest {
    @Test
    public void test1() throws Exception {
        DataRow row = new DataRow();
        row.put("a", "cyx");
        row.put("b", "cyx");
        row.put("c", "cyx");
        row.put("c", "cyx");
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", "xxx");
        map.put("d", "cyxo");
        row.putAll(map);
        System.out.println(row);
        System.out.println(row.containsValue("cyx"));
        System.out.println(row.put("x", "d"));
        System.out.println(ReflectUtil.obj2Json(row));
        DataRow jRow = DataRow.fromJson("{\n" +
                "  \"name\": \"chengyuxing\",\n" +
                "  \"age\": 28,\n" +
                "  \"address\": \"昆明市\",\n" +
                "  \"animals\": [\n" +
                "    \"dog\",\n" +
                "    \"cat\",\n" +
                "    \"tiger\"\n" +
                "  ]\n" +
                "}");
        System.out.println(jRow);
        System.out.println(jRow.size());
        System.out.println(jRow.getString("address"));
        System.out.println(jRow.getString(1));

        System.out.println(jRow.reduce(new ArrayList<>(), (acc, k, v) -> {
            if (v instanceof List) {
                ((List<?>) v).forEach(item -> acc.add(k + ":" + item));
            } else {
                acc.add(k + ":" + v);
            }
            return acc;
        }));
        System.out.println(jRow.replace("name", "cyx123"));
        System.out.println(jRow);
    }

    @Test
    public void testEach() throws Exception {
        DataRow row = DataRow.fromPair("name", "cyx", "age", 28, "address", "昆明市");
        row.forEach((k, v) -> System.out.println(k + ":" + v));
        System.out.println(row.toMap());
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            System.out.println(entry);
            if (entry.getKey().equals("age")) {
                entry.setValue(31);
            }
        }
        System.out.println(row);
    }
}
