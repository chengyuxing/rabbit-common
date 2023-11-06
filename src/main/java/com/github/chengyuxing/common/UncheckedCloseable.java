package com.github.chengyuxing.common;

/**
 * Unchecked closeable interface support nest closeable.
 */
public interface UncheckedCloseable extends Runnable, AutoCloseable {
    @Override
    default void run() {
        try {
            close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructed a new UncheckedCloseable with initial closeable.
     *
     * @param closeable 　closeable
     * @return UncheckedCloseable
     */
    static UncheckedCloseable wrap(AutoCloseable closeable) {
        return closeable::close;
    }

    /**
     * Nest a closeable object.<br>
     * By try-with-resource, close called from the inside out.
     *
     * @param closeable closeable
     * @return nested UncheckedCloseable
     */
    default UncheckedCloseable nest(AutoCloseable closeable) {
        return () -> {
            try (UncheckedCloseable ignored = this) {
                closeable.close();
            }
        };
    }
}
