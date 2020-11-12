package tests.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class User {
    private String name;
    private int id;
    private Integer age;
    private boolean enable;

    private String[] strs;

    private List<String> roles;

    private Map<String, Object> family;

    private List<Map<String,Object>> families;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", age=" + age +
                ", enable=" + enable +
                ", strs=" + Arrays.toString(strs) +
                ", roles=" + roles +
                ", family=" + family +
                ", families=" + families +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Map<String, Object> getFamily() {
        return family;
    }

    public void setFamily(Map<String, Object> family) {
        this.family = family;
    }

    public List<Map<String, Object>> getFamilies() {
        return families;
    }

    public void setFamilies(List<Map<String, Object>> families) {
        this.families = families;
    }

    public String[] getStrs() {
        return strs;
    }

    public void setStrs(String[] strs) {
        this.strs = strs;
    }
}
