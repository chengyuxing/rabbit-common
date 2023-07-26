package com.github.chengyuxing.common.script;

public final class Patterns {
    //language=RegExp
    public static final String PIPES_PATTERN = "(\\s*\\|\\s*[\\w_]+)*";
    //language=RegExp
    public static final String STRING_PATTERN = "'[^']*'|\"[^\"]*\"";
    //language=RegExp
    public static final String VAR_KEY_PATTERN = "[\\w_.]+";
}
