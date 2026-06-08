package com.github.chengyuxing.common.console;

import java.util.StringJoiner;

/**
 * Console printer.
 */
public class Printer {
    public static String colorful(String str, AnsiStyle... styles) {
        if (styles.length == 0) {
            return str;
        }
        StringJoiner codes = new StringJoiner(";");
        for (AnsiStyle style : styles) {
            codes.add(style.code());
        }
        return "\033[" + codes + "m" + str.replaceAll("\033\\[[\\d;]+m", "") + "\033[0m";
    }

    public static void print(String str, AnsiStyle style) {
        System.err.print(colorful(str, style));
    }

    public static void println(String str, AnsiStyle style) {
        System.err.println(colorful(str, style));
    }

    public static void printf(String str, AnsiStyle style, Object... args) {
        System.err.printf(colorful(str, style), args);
    }
}
