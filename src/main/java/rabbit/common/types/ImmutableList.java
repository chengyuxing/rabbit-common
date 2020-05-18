package rabbit.common.types;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 不可变集合工具类
 *
 * @param <T> 类型参数
 */
public final class ImmutableList<T> {
    private final T[] elements;

    ImmutableList(T... elements) {
        this.elements = elements;
    }

    public static <T> ImmutableList<T> of(T... more) {
        return new ImmutableList<>(more);
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> of(List<T> list) {
        return (ImmutableList<T>) of(list.toArray());
    }

    /**
     * 归并计算
     *
     * @param accumulator 累加器
     * @param init        初始值
     * @param <R>         结果类型参数
     * @return R
     */
    public <R> R reduce(BiFunction<R, T, R> accumulator, R init) {
        R acc = init;
        for (T element : elements) {
            acc = accumulator.apply(acc, element);
        }
        return acc;
    }

    public int sum(Function<T, Integer> action) {
        return reduce((acc, element) -> acc + action.apply(element), 0);
    }

    public void foreach(Consumer<T> action) {
        reduce((acc, element) -> {
            action.accept(element);
            return 0;
        }, 0);
    }
}
