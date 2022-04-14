package tests;

import com.github.chengyuxing.common.DataRow;
import org.junit.Test;

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
        System.out.println(row.getString("a869"));
    }

    @Test
    public void testMap() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < 10000; i++) {
            row.put("a" + i, i);
        }
        System.out.println(row.get("a780"));
    }
}
