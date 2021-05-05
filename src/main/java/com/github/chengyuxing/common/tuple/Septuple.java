package com.github.chengyuxing.common.tuple;

/**
 * 七元组
 *
 * @param <T1> 类型参数1
 * @param <T2> 类型参数2
 * @param <T3> 类型参数3
 * @param <T4> 类型参数4
 * @param <T5> 类型参数5
 * @param <T6> 类型参数6
 * @param <T7> 类型参数7
 */
public class Septuple<T1, T2, T3, T4, T5, T6, T7> extends Sextuple<T1, T2, T3, T4, T5, T6> {
    private final T7 item7;

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
}
