package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.script.pipe.builtin.Type;
import com.github.chengyuxing.common.tuple.Quintuple;
import com.github.chengyuxing.common.tuple.Triple;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.common.util.ValueUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MyTest {
    private static final OkHttpClient client = new OkHttpClient();

    static Map<String, Object> map = new HashMap<>();
    static DataRow row = DataRow.of();

    @BeforeClass
    public static void init() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("name", "chengyuxing");

        row = DataRow.of("a", 1,
                "b", 2,
                "name", "chengyuxing");
    }

    @Test
    public void testType() {
        Type type = new Type();
        System.out.println(type.transform("LocalDateTime.now()"));
        System.out.println(TokenType.CHECK_THROW);
        System.out.println(Long.parseLong("10009090909099099099"));
    }

    @Test
    public void testResourceIntercept() {
        new FileResource("http://localhost:8080/share/homebrew.md") {
            @Override
            protected @Nullable Supplier<InputStream> requestIntercept(final String path) {
                if (!path.endsWith("http:")) {
                    return null;
                }
                return () -> {
                    Request request = new Request.Builder().get().url(path).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            if (body != null) {
                                return body.byteStream();
                            }
                        }
                        throw new RuntimeException("");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
            }
        }.readLines(StandardCharsets.UTF_8)
                .forEach(System.out::println);
    }

    @Test
    public void testRequestIntercept() {

    }

    @Test
    public void test2() throws Exception {
        List<DataRow> rows = new ArrayList<>();
        rows.add(DataRow.of("a", 1, "b", "x"));
        rows.add(DataRow.of("a", 2, "b", "x1"));
        rows.add(DataRow.of("a", 3, "b", "x2"));
        rows.add(DataRow.of("a", 4, "b", "x3"));
        rows.add(DataRow.of("a", 5, "b", "x4"));

        DataRow row1 = DataRow.of().add("a", "b").add("b", "b");
        System.out.println(row1);

    }

    @Test
    public void rowTest2() throws Exception {
    }

    @Test
    public void mapTest() throws Exception {
//        DataRow row1 = DataRow.of("a", "chengyuxing", "b", "29");
//        DataRow row2 = DataRow.of("a", "chengyuxing", "b", "29");
//        System.out.println(Objects.equals(row1, row2));
//        System.out.println(row1.hashCode() + ":" + row2.hashCode());
//        KeyValue a = new KeyValue("name", "cyx");
//        KeyValue b = new KeyValue("name", "cyx");
//        System.out.println(Objects.equals(a, b));
//        System.out.println(a.hashCode() + ":" + b.hashCode());
//        Pair pair1 = Pair.of("a", "b");
//        Pair pair2 = Pair.of("a", "b");
//        System.out.println(pair2.equals(pair1));
//        System.out.println(pair2.hashCode() + ":" + pair1.hashCode());

        Triple triple1 = Triple.of("a", "b", "c");
        Triple triple2 = Triple.of("aa", "sb", "c");
        System.out.println(triple1.equals(triple2));
        System.out.println(triple1.hashCode() + ":" + triple2.hashCode());
    }

    @Test
    public void tsv() throws Exception {
        FileOutputStream csv = new FileOutputStream("/Users/chengyuxing/Downloads/lines.csv");
        FileOutputStream tsv = new FileOutputStream("/Users/chengyuxing/Downloads/lines.tsv");
        for (int i = 0; i < 10000; i++) {
            List<Object> row = new ArrayList<>();
            row.add("chengyuxing");
            row.add(i);
            row.add(Math.random() * 1000);
            row.add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            row.add("昆明市");
            row.add(i % 3 == 0 ? "" : "ok");
            row.add("530111199305107036");
//            Lines.writeLine(csv, row, ",");
//            Lines.writeLine(tsv, row, "\t");
        }
    }

    @Test
    public void pairTest() throws Exception {
        Quintuple<Integer, Integer, Integer, Integer, String> quintuple = Tuples.of(1, 2, 3, 4, "jackson");
        System.out.println(quintuple.getItem5());
    }

    @Test
    public void decodeTest() throws Exception {
        int money = 10000;
        Object v = ValueUtils.decode(money, 11, "穷光蛋",
                100, "西北风",
                1000, "打工族",
                10000, "小康生活",
                100000, "大老板",
                1000000, "MN", "LN");
        System.out.println(v);

//        System.out.println(ObjectUtil.nullable("null", "asd"));
    }

    @Test
    public void arrays() throws Exception {
        int sum = Stream.of(1, 2, 3, 4, 5, 6)
                .reduce(Integer::sum).get();
        System.out.println(sum);

    }

    @Test
    public void readFile() throws Exception {
        System.out.println((char) 58);
    }

    @Test
    public void javaBeanTest() throws IntrospectionException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        User user = new User();
        user.setName("chengyuxing");
        user.setAge(23);

    }
}
