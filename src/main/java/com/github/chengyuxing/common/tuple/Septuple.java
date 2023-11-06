package com.github.chengyuxing.common.tuple;

/**
 * Septuple
 *
 * @param <T1> item1 type
 * @param <T2> item2 type
 * @param <T3> item3 type
 * @param <T4> item4 type
 * @param <T5> item5 type
 * @param <T6> item6 type
 * @param <T7> item7 type
 */
public class Septuple<T1, T2, T3, T4, T5, T6, T7> extends Sextuple<T1, T2, T3, T4, T5, T6> {
    protected final T7 item7;

    Septuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7) {
        super(item1, item2, item3, item4, item5, item6);
        this.item7 = item7;
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Septuple<T1, T2, T3, T4, T5, T6, T7> of(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7) {
        return new Septuple<>(item1, item2, item3, item4, item5, item6, item7);
    }

    public T7 getItem7() {
        return item7;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ", " + item3 + ", " + item4 + ", " + item5 + ", " + item6 + ", " + item7 + ")";
    }
}
