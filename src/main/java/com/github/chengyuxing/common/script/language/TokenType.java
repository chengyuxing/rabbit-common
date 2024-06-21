package com.github.chengyuxing.common.script.language;

public enum TokenType {
    // Keywords
    IF, ELSE, ENDIF,
    SWITCH, CASE,
    CHOOSE, WHEN,
    DEFAULT, BREAK, END,
    FOR, END_FOR, FOR_OF, FOR_DELIMITER, FOR_OPEN, FOR_CLOSE,

    // Other tokens
    IDENTIFIER, STRING, NUMBER, VARIABLE_NAME,
    NEWLINE, EOF, UNKNOWN, PLAIN_TEXT
}
