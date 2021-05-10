package tests;

import com.github.chengyuxing.common.utils.ReflectUtil;
import org.junit.Test;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.DateTimes;
import com.github.chengyuxing.common.utils.StringUtil;

import java.time.Instant;
import java.util.*;
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
    public void dtTest() throws Exception {
        System.out.println(DateTimes.toLocalDateTime("20210201092132"));
        System.out.println("20210201092100".length());
    }

    @Test
    public void concat() throws Exception {
        DataRow row1 = DataRow.fromPair("a", 1, "b", 2);
        DataRow row2 = DataRow.fromPair("c", 11, "d", 23);

        System.out.println(row1.concat(row2).add("e", 90).add("f", 999));
        System.out.println(row1);
    }

    @Test
    public void removeElement() throws Exception {
        DataRow row = DataRow.fromPair("a", 1, "b", 2, "c", 3, "d", 4, "e", 5);
        System.out.println(row);
        System.out.println(row.remove("b"));
    }

    @Test
    public void StrTests() throws Exception {
        System.out.println(StringUtil.startsWithIgnoreCase("Select * from Test.user", "SELECT"));
    }

    @Test
    public void sss() throws Exception {
        String str = ";;;;;;;;]\n  \t\r   Select * from Test.user;;;\t   ;;";
//        System.out.println(StringUtil.trimStarts(str, ";", "\n", " ", "]", "\t", "\r"));
        System.out.println(StringUtil.trim(str, ";", "\n", "\t", "]", " ", "\r"));
    }

    @Test
    public void IdxOfI() throws Exception {
        String str = "Select * from Test.user";
        System.out.println(StringUtil.containsIgnoreCase(str, "TEST"));
        System.out.println(StringUtil.charEqualIgnoreCase('S', 's'));
        System.out.println(!StringUtil.containsAllIgnoreCase(str, "from", "user"));
        System.out.println(StringUtil.containsIgnoreCase(str, "我的"));
    }

    @Test
    public void sdfg() throws Exception {
        DataRow dataRow = DataRow.fromPair("name", "cyx", "age", 27, "address", "kunming");

        LinkedHashMap<String, Object> map = dataRow.reduce((acc, name, value) -> {
            acc.put(name, value);
            return acc;
        }, new LinkedHashMap<>());

        System.out.println(map);
    }

    @Test
    public void testArgs() throws Exception {
        DataRow dataRow = DataRow.fromPair("id", 2, "name", "cyx", "dt", Instant.now());
        String json = ReflectUtil.obj2Json(dataRow);
        System.out.println(json);

        System.out.println(ReflectUtil.json2Obj(json, DataRow.class));
    }

    public static void len(String... args) {
        System.out.println(args.length);
    }
}
