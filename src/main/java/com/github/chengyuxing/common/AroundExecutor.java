package com.github.chengyuxing.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * The AroundExecutor class is an abstract class designed to provide a framework for executing
 * operations with before and after actions. It is generic, allowing for the use of any type T as
 * a context for these operations.
 *
 * @param <T> the type of the context used for tracking
 */
public abstract class AroundExecutor<T> {
    protected abstract void before(@NotNull T context);

    protected abstract void after(@NotNull T context, @Nullable Throwable throwable);

    public final  <R> R call(@NotNull T context, @NotNull Function<T, R> func) {
        Throwable error = null;
        try {
            before(context);
            return func.apply(context);
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            after(context, error);
        }
    }
}