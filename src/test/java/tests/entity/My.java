package tests.entity;

import com.github.chengyuxing.common.anno.Alias;

@Alias("test.users")
public class My {
    @Alias("xm")
    private Integer name;
    @Alias("nl")
    private Integer age;

    @Override
    public String toString() {
        return "My{" +
                "name=" + name +
                ", age=" + age +
                '}';
    }

    public Integer getName() {
        return name;
    }

    public void setName(Integer name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
