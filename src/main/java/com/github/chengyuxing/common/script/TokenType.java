package com.github.chengyuxing.common.script;

public enum TokenType {
    // Keywords
    IF, ELSE,
    ENDIF,    // #fi
    SWITCH, CASE,
    CHOOSE, WHEN,
    DEFAULT, BREAK, END,
    FOR,
    END_FOR,   // #done
    FOR_OF, FOR_DELIMITER, FOR_OPEN, FOR_CLOSE,

    // Other tokens
    IDENTIFIER, STRING, NUMBER,
    VARIABLE_NAME,  // :name
    NEWLINE, EOF, UNKNOWN, PLAIN_TEXT, COMMA,
    PIPE_SYMBOL,    // |

    // Bool expression tokens
    LOGIC_OR,   // ||
    LOGIC_AND,  // &&
    LOGIC_NOT,   // !
    OPERATOR,    // > < >= <= = == != <> ~ !~ @ !@
    LPAREN,     // (
    RPAREN      // )
}
