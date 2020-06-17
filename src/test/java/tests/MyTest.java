package tests;

import org.junit.Test;
import rabbit.common.tuple.Quintuple;
import rabbit.common.tuple.Tuples;
import rabbit.common.types.ImmutableList;
import rabbit.common.utils.Exps;
import rabbit.common.io.TSVWriter;
import rabbit.common.utils.ResourceUtil;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MyTest {

    @Test
    public void tsv() throws Exception {
        TSVWriter writer = TSVWriter.of("/Users/chengyuxing/Downloads/bytes.tsv");
        for (int i = 0; i < 10000; i++) {
            List<Object> row = new ArrayList<>();
            row.add("chengyuxing");
            row.add(i);
            row.add(Math.random() * 1000);
            row.add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            row.add("昆明市");
            row.add(i % 3 == 0 ? "" : "ok");
            writer.writeLine(row);
        }
        writer.close();
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
        Object v = Exps.decode(money, 10, "穷光蛋",
                100, "西北风",
                1000, "打工族",
                10000, "小康生活",
                100000, "大老板",
                1000000, "MN", "LK");
        System.out.println(v);

        System.out.println(Exps.nullable("null", "asd"));
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
    public void readFile() throws Exception{
        URL url = ResourceUtil.classLoader().getResource("rabbit/common/tuple");
        System.out.println(Paths.get(url.toURI()));
    }
}
