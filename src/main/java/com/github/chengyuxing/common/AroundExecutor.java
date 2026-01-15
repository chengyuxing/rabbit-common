package com.github.chengyuxing.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * The AroundExecutor class is an abstract class designed to provide a framework for executing
 * operations with before and after actions. It is generic, allowing for the use of any type T as
 * an identifier for these operations.
 * <p>
 * Subclasses must implement the onStart and onStop methods, which are called before and after the
 * execution of a function, respectively. The call method is used to execute a provided function,
 * encapsulating it with the start and stop logic.
 *
 * @param <T> the type of the identifier used for tracking or identifying the operation
 */
public abstract class AroundExecutor<T> {
    protected abstract void onStart(@NotNull T identifier);

    protected abstract void onStop(@NotNull T identifier, @Nullable Object result, @Nullable Throwable throwable);

    public <R> R call(@NotNull T identify, @NotNull Function<T, R> func) {
        R result = null;
        Throwable error = null;
        try {
            onStart(identify);
            result = func.apply(identify);
            return result;
        } catch (Throwable throwable) {
            error = throwable;
            throw throwable;
        } finally {
            onStop(identify, result, error);
        }
    }
}