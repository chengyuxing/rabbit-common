package tests;

import com.github.chengyuxing.common.DataRow;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapTests {
    @Test
    public void testRow() throws Exception {
        DataRow row = DataRow.empty();
        for (int i = 0; i < 10000; i++) {
            row.put("a" + i, i);
        }
        System.out.println(row.getString(780));
    }

    @Test
    public void testMap() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < 10000; i++) {
            row.put("a" + i, i);
        }
        System.out.println(row.get("a780"));
    }

    //    @Test
    public void testdx() throws Exception {
        DataRow row2 = DataRow.of(new String[]{"a", "b", "c", "d"}, new Object[]{1, 2, true, LocalDateTime.now()});
        row2.add("name", "chengyuxing");
        System.out.println(row2.size());
        System.out.println(row2.getString("a"));
        System.out.println(row2.getValues());
        System.out.println(row2.getNames());
        System.out.println(row2.getType("a"));
        System.out.println(row2.remove("d"));
        System.out.println(row2.pick("a", "b"));
    }
}
