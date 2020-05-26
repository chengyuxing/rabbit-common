package rabbit.common.types;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * 不可变集合工具类
 *
 * @param <T> 类型参数
 */
public final class ImmutableList<T> {
    private final List<T> elements;

    ImmutableList(List<T> elements) {
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }

    public static <T> ImmutableList<T> of(List<T> list) {
        return new ImmutableList<>(list);
    }

    @SafeVarargs
    public static <T> ImmutableList<T> of(T... elements) {
        return of(Arrays.asList(elements));
    }

    public static <T> ImmutableList<T> empty() {
        return of(Collections.emptyList());
    }

    /**
     * 归并计算
     *
     * @param init   初始值
     * @param action 动作（累加器，当前值，单前索引）
     * @param <R>    结果类型参数
     * @return R
     */
    public <R> R reduce(TiFunction<R, T, Integer, R> action, R init) {
        R acc = init;
        for (int i = 0; i < elements.size(); i++) {
            acc = action.apply(acc, elements.get(i), i);
        }
        return acc;
    }

    /**
     * 归并计算
     *
     * @param action 动作（累加器，当前值）
     * @param init   初始值
     * @param <R>    结果类型参数
     * @return R
     */
    public <R> R reduce(BiFunction<R, T, R> action, R init) {
        return reduce((acc, current, index) -> action.apply(acc, current), init);
    }

    public ImmutableList<T> where(Predicate<T> predicate) {
        return where((current, index) -> predicate.test(current));
    }

    public ImmutableList<T> where(BiPredicate<T, Integer> predicate) {
        return of(reduce((acc, current, index) -> {
            if (predicate.test(current, index)) {
                acc.add(current);
            }
            return acc;
        }, new ArrayList<>()));
    }

    public Optional<T> findBy(Predicate<T> predicate) {
        T result = null;
        for (T t : elements) {
            if (predicate.test(t)) {
                result = t;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    public <K> ImmutableList<T> distinctBy(Function<T, K> action) {
        final Set<K> keys = new HashSet<>();
        List<T> result = reduce((acc, current) -> {
            K key = action.apply(current);
            if (!keys.contains(key)) {
                keys.add(key);
                acc.add(current);
            }
            return acc;
        }, new ArrayList<>());
        keys.clear();
        return of(result);
    }

    public <R> ImmutableList<R> map(Function<T, R> action) {
        return of(reduce((acc, current) -> {
            acc.add(action.apply(current));
            return acc;
        }, new ArrayList<>()));
    }

    public <K> Map<K, List<T>> groupBy(Function<T, K> action) {
        return reduce((acc, current) -> {
            K key = action.apply(current);
            if (!acc.containsKey(key)) {
                acc.put(key, new ArrayList<>());
            }
            acc.get(key).add(current);
            return acc;
        }, new HashMap<>());
    }

    public ImmutableList<List<T>> grouped(int size) {
        return of(reduce((acc, current, index) -> {
            List<T> group;
            if (index % size == 0) {
                group = new ArrayList<>();
                acc.add(group);
            }
            int newIndex = index / size;
            acc.get(newIndex).add(current);
            return acc;
        }, new ArrayList<>()));
    }

    public ImmutableList<T> slice(int start, int end) {
        List<T> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            result.add(elements.get(i));
        }
        return of(result);
    }

    public ImmutableList<T> concat(ImmutableList<T> that) {
        List<T> result = new ArrayList<>();
        result.addAll(elements);
        result.addAll(that.elements);
        return of(result);
    }

    public boolean includesBY(Function<T, Boolean> action) {
        return findBy(action::apply).isPresent();
    }

    public long sum(Function<T, Integer> action) {
        return reduce((acc, element) -> acc + action.apply(element), 0);
    }

    public long product(Function<T, Integer> action) {
        return reduce((acc, current) -> acc * action.apply(current), 1);
    }

    public void foreach(Consumer<T> action) {
        reduce((acc, element) -> {
            action.accept(element);
            return 0;
        }, 0);
    }

    public T head() {
        return elements.get(0);
    }

    public T last() {
        return elements.get(count() - 1);
    }

    public ImmutableList<T> tail() {
        return slice(1, elements.size());
    }

    public T get(int index) {
        return elements.get(index);
    }

    public boolean isEmpty() {
        return count() == 0;
    }

    public int count() {
        return elements.size();
    }

    public Stream<T> stream() {
        return elements.stream();
    }

    public List<T> toList() {
        return elements;
    }
}
