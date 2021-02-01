package tests;

import org.junit.Test;
import rabbit.common.io.FileResource;
import rabbit.common.types.DataRow;
import rabbit.common.utils.DateTimes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Test2 {

    static List<DataRow> rows = new ArrayList<>();
    static List<DataRow2> rows2 = new ArrayList<>();

    //    @BeforeClass
    @Test
    public void init() {
        for (int i = 0; i < 10000; i++) {
            DataRow row = DataRow.of(
                    new String[]{"a", "b", "c", "d", "e"},
                    new String[]{"String", "String", "String", "String", "String"},
                    new Object[]{1, 2, 3, 4, 5}
            );
            rows.add(row);
        }
    }

    @Test
    public void mp() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("x", "d");
        System.out.println(map.get("a"));
    }

    @Test
    public void d1() throws Exception {
        DataRow row = DataRow.of(
                new String[]{"a", "b", "c", "d", "e"},
                new String[]{"String", "Double", "String", "String", "String"},
                new Object[]{null, 2, "12", 4, 5}
        );
        System.out.println(row.getInt("k"));
    }

    @Test
    public void d2() throws Exception {
        DataRow2 row = DataRow2.of(
                new String[]{"a", "b", "c", "d", "e"},
                new String[]{"String", "Object", "String", "String", "String"},
                new Object[]{1, 2, 3, 4, 5}
        );
        System.out.println(row.getString(4));
    }

    @Test
    public void rTest() throws Exception {
        System.out.println(rows.get(111).getString("c"));
    }

    @Test
    public void speedTest2() throws Exception {
        List<Map<String, Object>> maps = rows.stream().map(r -> r.toMap(v -> v)).collect(Collectors.toList());
        System.out.println(maps.size());
    }

    @Test
    public void speedTest() throws Exception {
        List<Map<String, Object>> maps = rows.stream().map(DataRow::toMap).collect(Collectors.toList());
        System.out.println(maps.size());
    }

    @Test
    public void fileTest() throws Exception {
        FileResource resource = new FileResource("file:/Users/chengyuxing/Downloads/Bob.app.zip");
        System.out.println(resource.getURL());
    }

    @Test
    public void dtTest() throws Exception{
        System.out.println(DateTimes.toLocalDateTime("20210201092132"));
        System.out.println("20210201092100".length());
    }
}
