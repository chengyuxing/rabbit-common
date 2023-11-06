package com.github.chengyuxing.common;

/**
 * 3 input args function interface.
 *
 * @param <T1> arg1 type
 * @param <T2> arg2 type
 * @param <T3> arg3 type
 * @param <R>  result type
 */
@FunctionalInterface
public interface TiFunction<T1, T2, T3, R> {
    R apply(T1 t1, T2 t2, T3 t3);
}
