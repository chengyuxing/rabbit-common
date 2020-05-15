package org.rabbit.common.tuple;

/**
 * 六元组
 *
 * @param <T1> 类型参数1
 * @param <T2> 类型参数2
 * @param <T3> 类型参数3
 * @param <T4> 类型参数4
 * @param <T5> 类型参数5
 * @param <T6> 类型参数6
 */
public class Sextuple<T1, T2, T3, T4, T5, T6> extends Quintuple<T1, T2, T3, T4, T5> {
    private final T6 item6;

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
}
