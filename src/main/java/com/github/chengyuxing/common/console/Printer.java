package com.github.chengyuxing.common.console;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Console printer.
 */
public class Printer {
    public static String colorful(String str, AnsiStyle... styles) {
        if (styles.length == 0) {
            return str;
        }
        return beginStyle(styles) + removeStyle(str) + endStyle();
    }

    public static String beginStyle(AnsiStyle... styles) {
        if (styles.length == 0) {
            return "";
        }
        Set<String> codes = new LinkedHashSet<>();
        for (AnsiStyle style : styles) {
            codes.add(style.code());
        }
        return "\033[" + String.join(";", codes) + "m";
    }

    public static String endStyle() {
        return "\033[0m";
    }

    public static String removeStyle(String str) {
        return str.replaceAll("\033\\[[\\d;]+m", "");
    }

    public static void print(String str, AnsiStyle... styles) {
        System.err.print(colorful(str, styles));
    }

    public static void println(String str, AnsiStyle... styles) {
        System.err.println(colorful(str, styles));
    }

    public static void printf(String str, Object... args) {
        System.err.printf(str, args);
    }

    public static void printf(String str, AnsiStyle[] styles, Object... args) {
        System.err.printf(colorful(str, styles), args);
    }
}
