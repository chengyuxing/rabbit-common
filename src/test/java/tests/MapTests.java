package tests;

import com.github.chengyuxing.common.DataRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapTests {

    @Test
    public void testRow() throws Exception {
        DataRow row = DataRow.empty();
        for (int i = 0; i < 10000; i++) {
            row.put("a" + i, i);
        }
        System.out.println(row.getString("a869"));
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
