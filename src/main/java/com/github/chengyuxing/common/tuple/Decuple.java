package com.github.chengyuxing.common.tuple;

/**
 * Decuple
 *
 * @param <T1>  item1 type
 * @param <T2>  item2 type
 * @param <T3>  item3 type
 * @param <T4>  item4 type
 * @param <T5>  item5 type
 * @param <T6>  item6 type
 * @param <T7>  item7 type
 * @param <T8>  item8 type
 * @param <T9>  item9 type
 * @param <T10> item10 type
 */
public class Decuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> extends Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> {
    protected final T10 item10;

    Decuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8, T9 item9, T10 item10) {
        super(item1, item2, item3, item4, item5, item6, item7, item8, item9);
        this.item10 = item10;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Decuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> of(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8, T9 item9, T10 item10) {
        return new Decuple<>(item1, item2, item3, item4, item5, item6, item7, item8, item9, item10);
    }

    public T10 getItem10() {
        return item10;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ", " + item3 + ", " + item4 + ", " + item5 + ", " + item6 + ", " + item7 + ", " + item8 + ", " + item9 + ", " + item10 + ")";
    }
}
