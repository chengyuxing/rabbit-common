package com.github.chengyuxing.common.console;

import java.util.StringJoiner;

/**
 * Console printer.
 */
public class Printer {
    public static String colorful(String str, AnsiStyle... colors) {
        if (colors.length == 0) {
            return str;
        }
        StringJoiner codes = new StringJoiner(";");
        for (AnsiStyle color : colors) {
            codes.add(color.code());
        }
        return "\033[" + codes + "m" + str.replaceAll("\033\\[[\\d;]+m", "") + "\033[0m";
    }

    public static void print(String str, AnsiStyle color) {
        System.err.print(colorful(str, color));
    }

    public static void println(String str, AnsiStyle color) {
        System.err.println(colorful(str, color));
    }

    public static void printf(String str, AnsiStyle color, Object... args) {
        System.err.printf(colorful(str, color), args);
    }
}
