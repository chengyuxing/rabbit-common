package com.github.chengyuxing.common.console;

/**
 * Console printer.
 */
public class Printer {
    public static String colorful(String str, AnsiColor color) {
        return "\033[" + color.code() + "m" + str.replaceAll("\033\\[\\d+m", "") + "\033[0m";
    }

    public static String underline(String str) {
        return "\033[4m" + str + "\033[0m";
    }

    public static void print(String str, AnsiColor color) {
        System.out.print(colorful(str, color));
    }

    public static void println(String str, AnsiColor color) {
        System.out.println(colorful(str, color));
    }

    public static void printf(String str, AnsiColor color, Object... args) {
        System.out.printf(colorful(str, color), args);
    }
}
