package com.github.chengyuxing.common.tuple;

/**
 * Quintuple
 *
 * @param <T1>  item1 type
 * @param <T2>  item2 type
 * @param <T3>  item3 type
 * @param <T4>  item4 type
 * @param <T5>  item5 type
 */
public class Quintuple<T1, T2, T3, T4, T5> extends Quadruple<T1, T2, T3, T4> {
    protected final T5 item5;

    Quintuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
        super(item1, item2, item3, item4);
        this.item5 = item5;
    }

    public static <T1, T2, T3, T4, T5> Quintuple<T1, T2, T3, T4, T5> of(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
        return new Quintuple<>(item1, item2, item3, item4, item5);
    }

    public T5 getItem5() {
        return item5;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ", " + item3 + ", " + item4 + ", " + item5 + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quintuple)) return false;
        if (!super.equals(o)) return false;

        Quintuple<?, ?, ?, ?, ?> quintuple = (Quintuple<?, ?, ?, ?, ?>) o;

        return getItem5() != null ? getItem5().equals(quintuple.getItem5()) : quintuple.getItem5() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getItem5() != null ? getItem5().hashCode() : 0);
        return result;
    }
}
