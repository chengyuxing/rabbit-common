package com.github.chengyuxing.common;

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

    /**
     * 创建一个不可变集合
     *
     * @param list list
     * @param <T>  类型参数
     * @return 不可变集合
     */
    public static <T> ImmutableList<T> of(List<T> list) {
        return new ImmutableList<>(list);
    }

    /**
     * 创建一个不可变集合
     *
     * @param elements 一组元素
     * @param <T>      类型参数
     * @return 不可变集合
     */
    @SafeVarargs
    public static <T> ImmutableList<T> of(T... elements) {
        return of(Arrays.asList(elements));
    }

    /**
     * 创建一个空的不可变集合
     *
     * @param <T> 类型参数
     * @return 空的不可变集合
     */
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

    /**
     * 过滤操作
     *
     * @param predicate 断言
     * @return 过滤后的不可变集合
     */
    public ImmutableList<T> where(Predicate<T> predicate) {
        return where((current, index) -> predicate.test(current));
    }

    /**
     * 过滤操作
     *
     * @param predicate 断言
     * @return 过滤后的不可变集合
     */
    public ImmutableList<T> where(BiPredicate<T, Integer> predicate) {
        return of(reduce((acc, current, index) -> {
            if (predicate.test(current, index)) {
                acc.add(current);
            }
            return acc;
        }, new ArrayList<>()));
    }

    /**
     * 查找
     *
     * @param predicate 断言
     * @return 过滤后的不可变集合
     */
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

    /**
     * 去重复
     *
     * @param action 动作
     * @param <K>    根据去重复的的项
     * @return 去重后的不可变集合
     */
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

    /**
     * 映射
     *
     * @param action 动作
     * @param <R>    结果类型参数
     * @return 映射后新的不可变集合
     */
    public <R> ImmutableList<R> map(Function<T, R> action) {
        return of(reduce((acc, current) -> {
            acc.add(action.apply(current));
            return acc;
        }, new ArrayList<>()));
    }

    /**
     * 分组
     *
     * @param action 动作
     * @param <K>    根据分组的类型参数
     * @return 分组后的集合
     */
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

    /**
     * 分组
     *
     * @param size 分组大小
     * @return 分组后的不可变集合
     */
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

    /**
     * 分割
     *
     * @param start 开始元素
     * @param end   结束元素
     * @return 分割后的不可变集合
     */
    public ImmutableList<T> slice(int start, int end) {
        List<T> result = new ArrayList<>();
        for (int i = start; i < end; i++) {
            result.add(elements.get(i));
        }
        return of(result);
    }

    /**
     * 连接
     *
     * @param that 另一个不可变集合
     * @return 新的不可变集合
     */
    public ImmutableList<T> concat(ImmutableList<T> that) {
        List<T> result = new ArrayList<>();
        result.addAll(elements);
        result.addAll(that.elements);
        return of(result);
    }

    /**
     * 包含
     *
     * @param action 动作
     * @return 是否包含
     */
    public boolean includesBy(Function<T, Boolean> action) {
        return findBy(action::apply).isPresent();
    }

    /**
     * 求和
     *
     * @param action 动作
     * @return 和
     */
    public long sum(Function<T, Integer> action) {
        return reduce((acc, element) -> acc + action.apply(element), 0);
    }

    /**
     * 求积
     *
     * @param action 动作
     * @return 积
     */
    public long product(Function<T, Integer> action) {
        return reduce((acc, current) -> acc * action.apply(current), 1);
    }

    /**
     * 遍历
     *
     * @param action 动作
     */
    public void foreach(Consumer<T> action) {
        reduce((acc, element) -> {
            action.accept(element);
            return 0;
        }, 0);
    }

    /**
     * 获取第一个元素
     *
     * @return 第一个元素
     */
    public T head() {
        return elements.get(0);
    }

    /**
     * 获取最后一个元素
     *
     * @return 最后一个元素
     */
    public T last() {
        return elements.get(count() - 1);
    }

    /**
     * 获取尾巴
     *
     * @return 尾巴元素
     */
    public ImmutableList<T> tail() {
        return slice(1, elements.size());
    }

    /**
     * 根据索引获取一个元素
     *
     * @param index 索引
     * @return 元素
     */
    public T get(int index) {
        return elements.get(index);
    }

    /**
     * 空
     *
     * @return 是否空
     */
    public boolean isEmpty() {
        return count() == 0;
    }

    /**
     * 集合大小
     *
     * @return 元素个数
     */
    public int count() {
        return elements.size();
    }

    /**
     * 转为流
     *
     * @return 流
     */
    public Stream<T> stream() {
        return elements.stream();
    }

    /**
     * 转为列表
     *
     * @return 列表
     */
    public List<T> toList() {
        return new ArrayList<>(elements);
    }
}
