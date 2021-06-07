package com.github.chengyuxing.common.console;

/**
 * Linux/Unix系统终端颜色
 */
public enum Color {
    DARK_RED("31"),
    RED("91"),
    DARK_GREEN("32"),
    GREEN("92"),
    DARK_YELLOW("33"),
    YELLOW("93"),
    DARK_BLUE("34"),
    BLUE("94"),
    DARK_PURPLE("35"),
    PURPLE("95"),
    DARK_CYAN("36"),
    CYAN("96"),
    SILVER("37"),
    WHITE("97");

    private final String code;

    Color(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
