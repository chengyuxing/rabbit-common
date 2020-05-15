package com.github.chengyuxing.common.tuple;

/**
 * 元组
 */
public final class Tuples {
    public static <T1, T2> Pair<T1, T2> pair(T1 item1, T2 item2) {
        return Pair.of(item1, item2);
    }

    public static <T1, T2, T3> Triple<T1, T2, T3> triple(T1 item1, T2 item2, T3 item3) {
        return Triple.of(item1, item2, item3);
    }

    public static <T1, T2, T3, T4> Quadruple<T1, T2, T3, T4> quadruple(T1 item1, T2 item2, T3 item3, T4 item4) {
        return Quadruple.of(item1, item2, item3, item4);
    }

    public static <T1, T2, T3, T4, T5> Quintuple<T1, T2, T3, T4, T5> quintuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5) {
        return Quintuple.of(item1, item2, item3, item4, item5);
    }

    public static <T1, T2, T3, T4, T5, T6> Sextuple<T1, T2, T3, T4, T5, T6> sextuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6) {
        return Sextuple.of(item1, item2, item3, item4, item5, item6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> Septuple<T1, T2, T3, T4, T5, T6, T7> septuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7) {
        return Septuple.of(item1, item2, item3, item4, item5, item6, item7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> Octuple<T1, T2, T3, T4, T5, T6, T7, T8> octuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8) {
        return Octuple.of(item1, item2, item3, item4, item5, item6, item7, item8);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9> Nonuple<T1, T2, T3, T4, T5, T6, T7, T8, T9> nonuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8, T9 item9) {
        return Nonuple.of(item1, item2, item3, item4, item5, item6, item7, item8, item9);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Decuple<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> decuple(T1 item1, T2 item2, T3 item3, T4 item4, T5 item5, T6 item6, T7 item7, T8 item8, T9 item9, T10 item10) {
        return Decuple.of(item1, item2, item3, item4, item5, item6, item7, item8, item9, item10);
    }
}
