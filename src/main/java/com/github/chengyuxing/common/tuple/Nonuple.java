package com.github.chengyuxing.common.tuple;

/**
 * Nonuple
 *
 * @param <T1> item1 type
 * @param <T2> item2 type
 * @param <T3> item3 type
 * @param <T4> item4 type
 * @param <T5> item5 type
 * @param <T6> item6 type
 * @param <T7> item7 type
 * @param <T8> item8 type
 * @param <T9> item9 type
 */
public class Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends Octuple<T1, T2, T3, T4, T5, T6, T7, T8> {
    protected final T9 item9;

    Nonuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8, T9 item9) {
        super(item1, item2, item3, item4, item5, item6, item7, item8);
        this.item9 = item9;
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> of(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8, T9 item9) {
        return new Nonuple<>(item1, item2, item3, item4, item5, item6, item7, item8, item9);
    }

    public T9 getItem9() {
        return item9;
    }

    @Override
    public String toString() {
        return "(" + item1 + ", " + item2 + ", " + item3 + ", " + item4 + ", " + item5 + ", " + item6 + ", " + item7 + ", " + item8 + ", " + item9 + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nonuple)) return false;
        if (!super.equals(o)) return false;

        Nonuple<?, ?, ?, ?, ?, ?, ?, ?, ?> nonuple = (Nonuple<?, ?, ?, ?, ?, ?, ?, ?, ?>) o;

        return getItem9() != null ? getItem9().equals(nonuple.getItem9()) : nonuple.getItem9() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getItem9() != null ? getItem9().hashCode() : 0);
        return result;
    }
}
