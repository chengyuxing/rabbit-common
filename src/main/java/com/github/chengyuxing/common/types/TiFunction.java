package com.github.chengyuxing.common.types;

/**
 * 提供3个参数的函数接口
 *
 * @param <T1> 类型参数1
 * @param <T2> 类型参数2
 * @param <T3> 类型参数3
 * @param <R>  结果类型参数
 */
@FunctionalInterface
public interface TiFunction<T1, T2, T3, R> {
    R apply(T1 t1, T2 t2, T3 t3);
}
