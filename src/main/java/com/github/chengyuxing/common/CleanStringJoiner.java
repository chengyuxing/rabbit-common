package com.github.chengyuxing.common;

import com.github.chengyuxing.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

/**
 * A StringJoiner that excludes empty and null elements.
 */
public final class CleanStringJoiner {
    private final String delimiter;
    private final String prefix;
    private final String suffix;
    private StringJoiner joiner;

    public CleanStringJoiner(@NotNull String delimiter) {
        this(delimiter, "", "");
    }

    public CleanStringJoiner(@NotNull String delimiter, @NotNull String prefix, @NotNull String suffix) {
        this.delimiter = delimiter;
        this.prefix = prefix;
        this.suffix = suffix;
        this.joiner = new StringJoiner(delimiter, prefix, suffix);
    }

    public CleanStringJoiner add(String element) {
        if (!StringUtils.isEmpty(element)) {
            joiner.add(element);
        }
        return this;
    }

    public int length() {
        return joiner.length();
    }

    public boolean isEmpty() {
        return length() == 0;
    }

    public void clear() {
        this.joiner = new StringJoiner(delimiter, prefix, suffix);
    }

    @Override
    public String toString() {
        return joiner.toString();
    }
}
