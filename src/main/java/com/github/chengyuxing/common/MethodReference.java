package com.github.chengyuxing.common;

import java.io.Serializable;

/**
 * Class getter method reference.
 *
 * @param <T> entity type
 */
@FunctionalInterface
public interface MethodReference<T> extends Serializable {
    Object apply(T t);
}
