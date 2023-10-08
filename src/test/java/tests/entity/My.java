package tests.entity;

public class My {
    private Integer a;
    private Integer b;

    public boolean isOk() {
        return a + b > 0;
    }

    public Integer getA() {
        return a;
    }

    public void setA(Integer a) {
        this.a = a;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }
}
