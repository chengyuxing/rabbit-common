package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.DateTimes;
import com.github.chengyuxing.common.utils.Jackson;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.*;

public class NewRowTest {
    @Test
    public void test1() throws Exception {
        DataRow row = new DataRow(10);
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
        System.out.println(Jackson.toJson(row));
    }

    @Test
    public void testUpd() {
        DataRow row = DataRow.of("now", LocalDateTime.now(), "age", 28);
        row.<LocalDateTime>update("now", v -> DateTimes.of(v).toString("yyyy-MM-dd"));
        System.out.println(row);
    }

    @Test
    public void testEach() throws Exception {
        DataRow row = DataRow.of("name", "cyx", "age", 28, "address", Arrays.asList("china", "yunnan", "kunming"));
        Map<String, Object> newMap = row;
        newMap.remove("age");
        System.out.println(row);
        System.out.println(newMap);
        System.out.println(row.getString(0));
        System.out.println(row);
    }

    @Test
    public void testD() throws Exception {
        DataRow row = DataRow.of("name", null, "age", 28);
        row.put("address", "kunming");
        row.put("email", null);
        System.out.println(row.names().remove(1));
        System.out.println(row);
//        System.out.println(row.removeIfAbsent());
    }
}
