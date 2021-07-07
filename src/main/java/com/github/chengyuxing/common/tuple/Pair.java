package com.github.chengyuxing.common.tuple;

/**
 * 二元组
 *
 * @param <T1> 类型参数1
 * @param <T2> 类型参数2
 */
public class Pair<T1, T2> {
    protected final T1 item1;
    protected final T2 item2;

    Pair(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public static <T1, T2> Pair<T1, T2> of(T1 item1, T2 item2) {
        return new Pair<>(item1, item2);
    }

    public T1 getItem1() {
        return item1;
    }

    public T2 getItem2() {
        return item2;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ")";
    }
}
