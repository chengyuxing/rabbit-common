package org.rabbit.common.tuple;

/**
 * 三元组
 *
 * @param <T1> 类型参数1
 * @param <T2> 类型参数2
 * @param <T3> 类型参数3
 */
public class Triple<T1, T2, T3> extends Pair<T1, T2> {
    private final T3 item3;

    Triple(T1 item1, T2 item2, T3 item3) {
        super(item1, item2);
        this.item3 = item3;
    }

    public static <T1, T2, T3> Triple<T1, T2, T3> of(T1 item1, T2 item2, T3 item3) {
        return new Triple<>(item1, item2, item3);
    }

    public T3 getItem3() {
        return item3;
    }
}
