package com.github.chengyuxing.common.script;

public enum TokenType {
    // Keywords
    IF(Directives.IF), ELSE(Directives.ELSE), END_IF(Directives.FI),
    SWITCH(Directives.SWITCH), CASE(Directives.CASE),
    CHOOSE(Directives.CHOOSE), WHEN(Directives.WHEN),
    DEFAULT(Directives.DEFAULT), BREAK(Directives.BREAK), END(Directives.END),
    FOR(Directives.FOR),
    FOR_OF("of"), FOR_DELIMITER("delimiter"), FOR_OPEN("open"), FOR_CLOSE("close"),
    END_FOR(Directives.DONE),
    GUARD(Directives.GUARD), END_GUARD(Directives.THROW),
    CHECK(Directives.CHECK), CHECK_THROW("throw"),

    // Other tokens
    IDENTIFIER("<identifier>"), STRING("'<string>'"), NUMBER("<number>"),
    VARIABLE_NAME(":<variable name>"),
    NEWLINE("\\n"), EOF(""), UNKNOWN("<unknow>"), PLAIN_TEXT("<plain text>"),
    COMMA(","), COLON(":"),
    PIPE_SYMBOL("|"),
    AND_SYMBOL("&"),

    // Bool expression tokens
    LOGIC_OR("||"),
    LOGIC_AND("&&"),
    LOGIC_NOT("!"),
    OPERATOR(">, <, >=, <=, =, ==, !=, <>, ~, !~, @, !@"),
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}");

    private final String displayName;

    TokenType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return name() + " (" + displayName + ")";
    }
}
