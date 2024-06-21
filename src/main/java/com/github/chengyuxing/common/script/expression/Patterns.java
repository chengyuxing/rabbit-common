package com.github.chengyuxing.common.script.expression;

public final class Patterns {
    //language=RegExp
    /**
     * e.g. {@code | upper | length}
     */
    public static final String PIPES_PATTERN = "(\\s*\\|\\s*\\w+)*";
    //language=RegExp
    /**
     * e.g. {@code 'abc' "abc"}
     */
    public static final String STRING_PATTERN = "'[^']*'|\"[^\"]*\"";
    //language=RegExp
    /**
     * e.g. {@code abc a_bc a_1 a.b a.b.c _abc}
     */
    public static final String VAR_KEY_PATTERN = "[a-zA-Z_][\\w.]*";
    //language=RegExp
    /**
     * e.g. {@code blank true false null 3.14 12 a_b abc}
     */
    public static final String VAR_PATTERN = "[\\w.]+";
}
