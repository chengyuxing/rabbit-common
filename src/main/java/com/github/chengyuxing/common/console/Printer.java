package com.github.chengyuxing.common.console;

/**
 * Unix-Like控制台打印帮助类
 */
public class Printer {
    public static String colorful(String str, Color color) {
        return "\033[" + color.getCode() + "m" + str + "\033[0m";
    }

    public static String underline(String str) {
        return "\33[4m" + str + "\33[0m";
    }

    public static void print(String str, Color color) {
        System.out.print(colorful(str, color));
    }

    public static void println(String str, Color color) {
        System.out.println(colorful(str, color));
    }

    public static void printf(String str, Color color, Object... args) {
        System.out.printf(colorful(str, color), args);
    }
}
