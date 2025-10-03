package com.github.chengyuxing.common;

import java.util.StringJoiner;

/**
 * A StringJoiner that excludes empty and null elements.
 */
public final class CleanStringJoiner {
    private final StringJoiner joiner;

    public CleanStringJoiner(String delimiter) {
        this.joiner = new StringJoiner(delimiter);
    }

    public CleanStringJoiner add(String element) {
        if (element != null && !element.trim().isEmpty()) {
            joiner.add(element);
        }
        return this;
    }

    @Override
    public String toString() {
        return joiner.toString();
    }
}
