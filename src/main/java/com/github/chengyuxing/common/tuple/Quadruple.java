package com.github.chengyuxing.common.tuple;

/**
 * Quadruple
 *
 * @param <T1> item1 type
 * @param <T2> item2 type
 * @param <T3> item3 type
 * @param <T4> item4 type
 */
public class Quadruple<T1, T2, T3, T4> extends Triple<T1, T2, T3> {
    protected final T4 item4;

    Quadruple(T1 item1, T2 item2, T3 item3, T4 item4) {
        super(item1, item2, item3);
        this.item4 = item4;
    }

    public static <T1, T2, T3, T4> Quadruple<T1, T2, T3, T4> of(T1 item1, T2 item2, T3 item3, T4 item4) {
        return new Quadruple<>(item1, item2, item3, item4);
    }

    public T4 getItem4() {
        return item4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quadruple)) return false;
        if (!super.equals(o)) return false;

        Quadruple<?, ?, ?, ?> quadruple = (Quadruple<?, ?, ?, ?>) o;

        return getItem4() != null ? getItem4().equals(quadruple.getItem4()) : quadruple.getItem4() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getItem4() != null ? getItem4().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ", " + item3 + ", " + item4 + ")";
    }
}
