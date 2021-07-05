package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.ImmutableList;
import com.github.chengyuxing.common.io.LineIO;
import com.github.chengyuxing.common.tuple.Quintuple;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.common.utils.ObjectUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MyTest {

    static Map<String, Object> map = new HashMap<>();
    static DataRow row = DataRow.empty();

    @BeforeClass
    public static void init() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("name", "chengyuxing");

        row = DataRow.fromPair("a", 1,
                "b", 2,
                "name", "chengyuxing");
    }

    @Test
    public void rowTest2() throws Exception {
        System.out.println(row.toMap());
    }

    @Test
    public void mapTest() throws Exception {
        System.out.println(DataRow.fromMap(map));
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
            LineIO.writeLine(csv, row, ",");
            LineIO.writeLine(tsv, row, "\t");
        }
    }

    @Test
    public void read() throws Exception {
        LineIO.readLines(Paths.get("/Users/chengyuxing/Downloads/lines.tsv"), "\t")
                .limit(10)
                .forEach(System.out::println);

        LineIO.readLines(new FileInputStream("/Users/chengyuxing/Downloads/lines.tsv"), "\t")
                .skip(10)
                .limit(10)
                .forEach(System.out::println);
    }

    @Test
    public void ImmutableTest() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        ImmutableList<String> immutableList = ImmutableList.of(list);

        list.add("d");

        immutableList.tail().foreach(System.out::println);
    }

    @Test
    public void pairTest() throws Exception {
        Quintuple<Integer, Integer, Integer, Integer, String> quintuple = Tuples.quintuple(1, 2, 3, 4, "jackson");
        System.out.println(quintuple.getItem5());
    }

    @Test
    public void decodeTest() throws Exception {
        int money = 10000800;
        Object v = ObjectUtil.decode(money, 10, "穷光蛋",
                100, "西北风",
                1000, "打工族",
                10000, "小康生活",
                100000, "大老板",
                1000000, "MN", "LK");
        System.out.println(v);

        System.out.println(ObjectUtil.nullable("null", "asd"));
    }

    @Test
    public void listIm() throws Exception {
        long i = ImmutableList.of(1, 2, 3, 4, 5, 6).product(item -> item);
        System.out.println(i);

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
