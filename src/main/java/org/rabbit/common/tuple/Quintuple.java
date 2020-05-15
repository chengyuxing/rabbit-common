package org.rabbit.common.tuple;

/**
 * 五元组
 *
 * @param <T1> 类型参数1
 * @param <T2> 类型参数2
 * @param <T3> 类型参数3
 * @param <T4> 类型参数4
 * @param <T5> 类型参数5
 */
public class Quintuple<T1, T2, T3, T4, T5> extends Quadruple<T1, T2, T3, T4> {
    private final T5 item5;

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
}
