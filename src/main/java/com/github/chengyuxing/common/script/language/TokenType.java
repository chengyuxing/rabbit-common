package com.github.chengyuxing.common.script.language;

public enum TokenType {
    // Keywords
    IF, ELSE, ENDIF,
    SWITCH, CASE, DEFAULT, BREAK, END,
    CHOOSE, WHEN,
    FOR, ENDFOR, OF, DELIMITER, OPEN, CLOSE,

    // Other tokens
    IDENTIFIER, STRING, NUMBER, NAMED_PARAMETER,
    NEWLINE, EOF, UNKNOWN, PLAIN_TEXT
}
