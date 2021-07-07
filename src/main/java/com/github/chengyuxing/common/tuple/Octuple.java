package com.github.chengyuxing.common.tuple;

/**
 * 八元组
 *
 * @param <T1> 类型参数1
 * @param <T2> 类型参数2
 * @param <T3> 类型参数3
 * @param <T4> 类型参数4
 * @param <T5> 类型参数5
 * @param <T6> 类型参数6
 * @param <T7> 类型参数7
 * @param <T8> 类型参数8
 */
public class Octuple<T1, T2, T3, T4, T5, T6, T7, T8> extends Septuple<T1, T2, T3, T4, T5, T6, T7> {
    protected final T8 item8;

    Octuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8) {
        super(item1, item2, item3, item4, item5, item6, item7);
        this.item8 = item8;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Octuple<T1, T2, T3, T4, T5, T6, T7, T8> of(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8) {
        return new Octuple<>(item1, item2, item3, item4, item5, item6, item7, item8);
    }

    public T8 getItem8() {
        return item8;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ", " + item3 + ", " + item4 + ", " + item5 + ", " + item6 + ", " + item7 + ", " + item8 + ")";
    }
}
