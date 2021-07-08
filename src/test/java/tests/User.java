package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.common.utils.ReflectUtil;
import org.junit.Test;
import org.nutz.json.Json;

import java.time.LocalDateTime;
import java.util.Arrays;

public class User {
    private String name;
    private String address;
    private int age;
    private boolean old;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isOld() {
        return old;
    }

    public void setOld(boolean old) {
        this.old = old;
    }


    @Test
    public void x() throws Exception {
        DataRow row = DataRow.fromPair("name", "cyx", "age", 29, "now", LocalDateTime.now().toString(), "x", Arrays.asList(1, 2, 3, 4), "bytea", new byte[]{-1, 2, -10, 3});
        System.out.println(row);
        System.out.println(ReflectUtil.obj2Json(row));
        System.out.println(Json.toJson(row));
        System.out.println(ReflectUtil.obj2Json(Tuples.triple("a", "b", LocalDateTime.now().toString())));
    }

}
