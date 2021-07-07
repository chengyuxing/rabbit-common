package tests;

import org.junit.Test;

import java.util.*;

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
        Map<String, Object> map = new HashMap<>();
        map.put("name", "cyx");
        List<Object> l = new ArrayList<>();
        l.add("ages");
        l.add(map);
        List<Object> list = Collections.unmodifiableList(l);
        map.put("age", 28);
        System.out.println(list);
    }

}
