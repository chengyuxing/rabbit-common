package com.github.chengyuxing.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

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