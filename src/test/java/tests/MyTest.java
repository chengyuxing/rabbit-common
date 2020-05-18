package tests;

import org.junit.Test;
import rabbit.common.tuple.Quintuple;
import rabbit.common.tuple.Tuples;
import rabbit.common.types.ImmutableList;
import rabbit.common.utils.Exps;

import java.util.stream.Stream;

public class MyTest {
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
        int i = ImmutableList.of(1, 2, 3, 4, 5, 6)
                .sum(item -> item);
        System.out.println(i);

    }

    @Test
    public void arrays() throws Exception {
        int sum = Stream.of(1, 2, 3, 4, 5, 6)
                .reduce(Integer::sum).get();
        System.out.println(sum);

    }
}
