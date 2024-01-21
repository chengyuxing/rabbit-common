package com.github.chengyuxing.common.tuple;

/**
 * Triple
 *
 * @param <T1> item1 type
 * @param <T2> item2 type
 * @param <T3> item3 type
 */
public class Triple<T1, T2, T3> extends Pair<T1, T2> {
    protected final T3 item3;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Triple)) return false;
        if (!super.equals(o)) return false;

        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;

        return getItem3() != null ? getItem3().equals(triple.getItem3()) : triple.getItem3() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getItem3() != null ? getItem3().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ", " + item3 + ")";
    }
}
