package rabbit.common.tuple;

/**
 * 四元组
 *
 * @param <T1> 类型参数1
 * @param <T2> 类型参数2
 * @param <T3> 类型参数3
 * @param <T4> 类型参数4
 */
public class Quadruple<T1, T2, T3, T4> extends Triple<T1, T2, T3> {
    private final T4 item4;

    Quadruple(T1 item1, T2 item2, T3 item3, T4 item4) {
        super(item1, item2, item3);
        this.item4 = item4;
    }

    public static <T1, T2, T3, T4> Quadruple<T1, T2, T3, T4> of(T1 item1, T2 item2, T3 item3, T4 item4) {
        return new Quadruple<>(item1, item2, item3, item4);
    }

    public T4 getItem4() {
        return item4;
    }
}
