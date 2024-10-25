package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.MapExtends;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.util.*;

public class MapTests {

    @Test
    public void testRow() throws Exception {
        DataRow row = DataRow.of();
        for (int i = 0; i < 10000; i++) {
            row.put("a" + i, i);
        }
        System.out.println(row.getString("a869"));
    }

    @Test
    public void testzz() throws Exception {
        DataRow row = DataRow.of("Name", "cyx", "age", 27);
    }

    @Test
    public void testaa() throws Exception {
        class MyMap extends HashMap<String, Object> implements MapExtends<MyMap, Object> {
        }
        MyMap map = new MyMap();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
    }

    @Test
    public void testss() throws Exception {
        System.out.println(StringUtil.containsIgnoreCase("axjHd7DugC", "jhd"));
    }

    @Test
    public void testMap() throws Exception {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < 10000; i++) {
            row.put("a" + i, i);
        }
        System.out.println(row.get("a780"));
    }

    //    @Test
    public void testList() throws Exception {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add("a" + i);
            list.indexOf("a" + i);
        }
        System.out.println(list.indexOf("a7800"));
    }

    //    @Test
    public void testArray() throws Exception {
        String[] strings = new String[10000];
        for (int i = 0; i < 10000; i++) {
            strings[i] = "a" + i;
        }
        System.out.println(indexOf(strings, "a7800"));
    }

    public static int indexOf(String[] arr, String a) {
        for (int i = 0; i < arr.length; i++) {
            if (a.equals(arr[i])) {
                return i;
            }
        }
        return -1;
    }
}
