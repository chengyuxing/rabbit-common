package com.github.chengyuxing.common.tuple;

/**
 * Pair
 *
 * @param <T1> item1 type
 * @param <T2> item2 type
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (getItem1() != null ? !getItem1().equals(pair.getItem1()) : pair.getItem1() != null) return false;
        return getItem2() != null ? getItem2().equals(pair.getItem2()) : pair.getItem2() == null;
    }

    @Override
    public int hashCode() {
        int result = getItem1() != null ? getItem1().hashCode() : 0;
        result = 31 * result + (getItem2() != null ? getItem2().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ")";
    }
}
