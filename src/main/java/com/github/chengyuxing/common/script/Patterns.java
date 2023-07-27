package com.github.chengyuxing.common.script;

public final class Patterns {
    //language=RegExp
    public static final String PIPES_PATTERN = "(\\s*\\|\\s*\\w+)*";
    //language=RegExp
    public static final String STRING_PATTERN = "'[^']*'|\"[^\"]*\"";
    //language=RegExp
    public static final String VAR_KEY_PATTERN = "[a-zA-Z_][\\w.]*";
    // blank true false null 3.14 12 a_b abc
    //language=RegExp
    public static final String VAR_PATTERN = "[\\w.]+";
}
