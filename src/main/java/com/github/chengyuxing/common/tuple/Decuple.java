package com.github.chengyuxing.common.tuple;

/**
 * 十元组
 *
 * @param <T1>  类型参数1
 * @param <T2>  类型参数2
 * @param <T3>  类型参数3
 * @param <T4>  类型参数4
 * @param <T5>  类型参数5
 * @param <T6>  类型参数6
 * @param <T7>  类型参数7
 * @param <T8>  类型参数8
 * @param <T9>  类型参数9
 * @param <T10> 类型参数10
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
