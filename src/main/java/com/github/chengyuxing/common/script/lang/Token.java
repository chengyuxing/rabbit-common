package com.github.chengyuxing.common.script.lang;

public final class Token {
    private final TokenType type;
    private final String value;
    private int line = -1;
    private int column = -1;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        String s = type.name() + " (" + value.replace("\n", "\\n") + ")";
        if (line != -1 && column != -1) {
            s += " at line " + (line + 1) + ", column " + (column + 1);
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return getType() == token.getType() && getValue().equals(token.getValue());
    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + getValue().hashCode();
        return result;
    }
}
