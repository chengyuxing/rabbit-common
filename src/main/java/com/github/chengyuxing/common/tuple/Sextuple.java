package com.github.chengyuxing.common.tuple;

/**
 * Sextuple
 *
 * @param <T1> item1 type
 * @param <T2> item2 type
 * @param <T3> item3 type
 * @param <T4> item4 type
 * @param <T5> item5 type
 * @param <T6> item6 type
 */
public class Sextuple<T1, T2, T3, T4, T5, T6> extends Quintuple<T1, T2, T3, T4, T5> {
    protected final T6 item6;

    Sextuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6) {
        super(item1, item2, item3, item4, item5);
        this.item6 = item6;
    }

    public static <T1, T2, T3, T4, T5, T6> Sextuple<T1, T2, T3, T4, T5, T6> of(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6) {
        return new Sextuple<>(item1, item2, item3, item4, item5, item6);
    }

    public T6 getItem6() {
        return item6;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ", " + item3 + ", " + item4 + ", " + item5 + ", " + item6 + ")";
    }
}
