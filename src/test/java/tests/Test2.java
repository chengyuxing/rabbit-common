package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.DateTimes;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.utils.Jackson;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class Test2 {

    static List<DataRow> rows = new ArrayList<>();

    //    @BeforeClass
    @Test
    public void init() {
        for (int i = 0; i < 10000; i++) {
            DataRow row = DataRow.of(
                    new String[]{"a", "b", "c", "d", "e"},
                    new Object[]{1, 2, 3, 4, 5}
            );
            rows.add(row);
        }
    }

    public static class Now {
        private Date a;
        private LocalDateTime b;

        @Override
        public String toString() {
            return "Now{" +
                    "a=" + a +
                    ", b=" + b +
                    '}';
        }

        public LocalDateTime getB() {
            return b;
        }

        public void setB(LocalDateTime b) {
            this.b = b;
        }

        public Date getA() {
            return a;
        }

        public void setA(Date a) {
            this.a = a;
        }
    }

    @Test
    public void testdd() throws Exception {
        System.out.println(DataRow.of("a", "2022-12-23", "b", "2022-12-01").toEntity(Now.class));
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
                new Object[]{null, 2, "12", 4, 5}
        );
        System.out.println(row.getInt("k"));
    }

    @Test
    public void rTest() throws Exception {
        System.out.println(rows.get(111).getString("c"));
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
    public void testDt2() throws Exception {
        System.out.println(DateTimes.createISODateTime("2019-09-26T03:45:36.656Z"));
        System.out.println(DateTimes.createISODateTime("2019-09-26T03:45:36.656-0800"));
        System.out.println(DateTimes.createISODateTime("2019-09-25T18:00:14"));
        System.out.println(DateTimes.createISODateTime("2019-09-25T18:00:14z"));
        System.out.println(DateTimes.createISODateTime("2019-09-25 18:00:14"));

        System.out.println(LocalDateTime.now());

        System.out.println(DateTimes.toLocalDateTime("2019-09-26T03:45:36.656-0800"));
        System.out.println(DateTimes.currentTimestamp());

        System.out.println(DateTimes.toLocalDateTime("2021年12月23日"));

        System.out.println(DateTimes.toLocalDateTime("Wed, 04 Jan 2023 09:36:48 GMT"));

        System.out.println(DateTimes.toLocalDateTime("Wed Jan 04 18:52:01 CST 2023"));
        System.out.println(DateTimes.toLocalDateTime("Wed Jan 04 2023 17:36:48 GMT+0800"));

        System.out.println(DateTimes.createRFCLikeDateTime("Wed Jan 04 2023 17:36:48 GMT+0800"));

        System.out.println(new Date());

    }

    @Test
    public void test23() {
        System.out.println(DateTimes.of("2019-09-26"));
        System.out.println(DateTimes.of("2019-09-26T03:45:36.656-08"));
        System.out.println(DateTimes.of("2019-09-26T03:45:36.656-080053").toString("yyyy-MM-dd hh:mm:ss"));
        System.out.println(ZoneId.of("+08:00:54"));
    }

    @Test
    public void testdtM() throws Exception {
    }

    @Test
    public void concat() throws Exception {
        DataRow row1 = DataRow.of("a", 1, "b", 2);
        DataRow row2 = DataRow.of("c", 11, "d", 23);

        System.out.println(row1);
    }

    @Test
    public void removeElement() throws Exception {
        DataRow row = DataRow.of("a", 1, "b", 2, "c", 3, "d", 4, "e", 5);
        System.out.println(row);
        System.out.println(row.remove("b"));
        System.out.println(row.put("c", 109));
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
        DataRow dataRow = DataRow.of("name", "cyx", "age", 27, "address", "kunming");

        LinkedHashMap<String, Object> map = dataRow.reduce(new LinkedHashMap<>(), (acc, name, value) -> {
            acc.put(name, value);
            return acc;
        });

        System.out.println(map);
    }

    @Test
    public void testArgs() throws Exception {
        DataRow dataRow = DataRow.of("id", 2, "name", "cyx", "dt", Instant.now());
        String json = Jackson.toJson(dataRow);
        System.out.println(json);

        System.out.println(Jackson.toObject(json, DataRow.class));
    }

    @Test
    public void testlMap() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("c", 1);
        map.put("b", 1);
        map.put("a", 1);
        map.put("d", 1);
        System.out.println(map);
        map.put("a", 2);
        map.put("d", 21);
        map.put("e", 34);
        System.out.println(map);
    }

    public static void len(String... args) {
        System.out.println(args.length);
    }
}
