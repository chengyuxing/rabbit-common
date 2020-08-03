package tests;

import org.junit.BeforeClass;
import org.junit.Test;
import rabbit.common.types.DataRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Test2 {

    static List<DataRow> rows = new ArrayList<>();

    @BeforeClass
    public static void init() {
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
    public void speedTest2() throws Exception {
        List<Map<String, Object>> maps = rows.stream().map(r -> r.toMap2(v -> v)).collect(Collectors.toList());
        System.out.println(maps.size());
    }

    @Test
    public void speedTest() throws Exception {
        List<Map<String, Object>> maps = rows.stream().map(DataRow::toMap).collect(Collectors.toList());
        System.out.println(maps.size());
    }
}