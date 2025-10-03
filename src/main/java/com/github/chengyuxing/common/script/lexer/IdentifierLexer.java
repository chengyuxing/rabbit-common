package com.github.chengyuxing.common.script.lexer;

import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.chengyuxing.common.script.Directives.*;

/**
 * <h2>Identifier Lexer</h2>
 */
public class IdentifierLexer {
    private final String input;
    private final int line;
    private int position;
    private final int length;

    public IdentifierLexer(String input, int line) {
        this.input = input;
        this.line = line;
        this.position = 0;
        this.length = input.length();
    }

    private char currentChar() {
        return position < length ? input.charAt(position) : '\n';
    }

    private void advance() {
        position++;
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(currentChar()) && currentChar() != '\n') {
            advance();
        }
    }

    private String readWhile(Predicate<Character> predicate) {
        StringBuilder sb = new StringBuilder();
        while (position < length && predicate.test(currentChar())) {
            sb.append(currentChar());
            advance();
        }
        return sb.toString();
    }

    private boolean match(String s) {
        if (input.startsWith(s, position)) {
            position += s.length();
            return true;
        }
        return false;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (position < length) {
            skipWhitespace();
            char current = currentChar();
            if (current == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line, position));
                advance();
            } else if (current == '#') {
                advance();
                String keyword = readWhile(Character::isAlphabetic);
                switch (keyword.toLowerCase()) {
                    case "if":
                        tokens.add(new Token(TokenType.IF, IF, line, position));
                        break;
                    case "else":
                        tokens.add(new Token(TokenType.ELSE, ELSE, line, position));
                        break;
                    case "fi":
                        tokens.add(new Token(TokenType.END_IF, FI, line, position));
                        break;
                    case "end":
                        tokens.add(new Token(TokenType.END, END, line, position));
                        break;
                    case "switch":
                        tokens.add(new Token(TokenType.SWITCH, SWITCH, line, position));
                        break;
                    case "case":
                        tokens.add(new Token(TokenType.CASE, CASE, line, position));
                        break;
                    case "default":
                        tokens.add(new Token(TokenType.DEFAULT, DEFAULT, line, position));
                        break;
                    case "break":
                        tokens.add(new Token(TokenType.BREAK, BREAK, line, position));
                        break;
                    case "choose":
                        tokens.add(new Token(TokenType.CHOOSE, CHOOSE, line, position));
                        break;
                    case "when":
                        tokens.add(new Token(TokenType.WHEN, WHEN, line, position));
                        break;
                    case "for":
                        tokens.add(new Token(TokenType.FOR, FOR, line, position));
                        break;
                    case "done":
                        tokens.add(new Token(TokenType.END_FOR, DONE, line, position));
                        break;
                    case "guard":
                        tokens.add(new Token(TokenType.GUARD, GUARD, line, position));
                        break;
                    case "throw":
                        tokens.add(new Token(TokenType.END_GUARD, THROW, line, position));
                        break;
                    case "check":
                        tokens.add(new Token(TokenType.CHECK, CHECK, line, position));
                        break;
                    default:
                        tokens.add(new Token(TokenType.UNKNOWN, '#' + keyword, line, position));
                        break;
                }
            } else if (current == '\'') {
                advance();
                String str = readWhile(c -> c != '\'');
                advance();
                tokens.add(new Token(TokenType.STRING, '\'' + str + '\'', line, position));
            } else if (current == '"') {
                advance();
                String str = readWhile(c -> c != '"');
                advance();
                tokens.add(new Token(TokenType.STRING, '"' + str + '"', line, position));
            } else if (current == ',') {
                tokens.add(new Token(TokenType.COMMA, ",", line, position));
                advance();
            } else if (current == '|') {
                if (match("||")) {
                    tokens.add(new Token(TokenType.LOGIC_OR, "||", line, position));
                } else {
                    tokens.add(new Token(TokenType.PIPE_SYMBOL, "|", line, position));
                    advance();
                }
            } else if (current == '&') {
                if (match("&&")) {
                    tokens.add(new Token(TokenType.LOGIC_AND, "&&", line, position));
                } else {
                    tokens.add(new Token(TokenType.AND_SYMBOL, "&", line, position));
                    advance();
                }
            } else if (current == '!') {
                if (match("!=")) {
                    tokens.add(new Token(TokenType.OPERATOR, "!=", line, position));
                } else if (match("!~")) {
                    tokens.add(new Token(TokenType.OPERATOR, "!~", line, position));
                } else if (match("!@")) {
                    tokens.add(new Token(TokenType.OPERATOR, "!@", line, position));
                } else {
                    tokens.add(new Token(TokenType.LOGIC_NOT, "!", line, position));
                    advance();
                }
            } else if (current == '=') {
                if (match("==")) {
                    tokens.add(new Token(TokenType.OPERATOR, "==", line, position));
                } else {
                    tokens.add(new Token(TokenType.OPERATOR, "=", line, position));
                    advance();
                }
            } else if (current == '>') {
                if (match(">=")) {
                    tokens.add(new Token(TokenType.OPERATOR, ">=", line, position));
                } else {
                    tokens.add(new Token(TokenType.OPERATOR, ">", line, position));
                    advance();
                }
            } else if (current == '<') {
                if (match("<=")) {
                    tokens.add(new Token(TokenType.OPERATOR, "<=", line, position));
                } else if (match("<>")) {
                    tokens.add(new Token(TokenType.OPERATOR, "<>", line, position));
                } else {
                    tokens.add(new Token(TokenType.OPERATOR, "<", line, position));
                    advance();
                }
            } else if (current == '~') {
                tokens.add(new Token(TokenType.OPERATOR, "~", line, position));
                advance();
            } else if (current == '@') {
                tokens.add(new Token(TokenType.OPERATOR, "@", line, position));
                advance();
            } else if (current == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", line, position));
                advance();
            } else if (current == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", line, position));
                advance();
            } else if (current == '{') {
                tokens.add(new Token(TokenType.LBRACE, "{", line, position));
                advance();
            } else if (current == '}') {
                tokens.add(new Token(TokenType.RBRACE, "}", line, position));
                advance();
            } else if (current == ':') {
                advance();
                String str = readWhile(c -> Character.isLetterOrDigit(c) || c == '_' || c == '.');
                if (!str.isEmpty()) {
                    tokens.add(new Token(TokenType.VARIABLE_NAME, ':' + str, line, position));
                } else {
                    tokens.add(new Token(TokenType.COLON, ":", line, position));
                }
            } else if (Character.isAlphabetic(current)) {
                String identifier = readWhile(c -> Character.isLetterOrDigit(c) || c == '_' || c == '.');
                switch (identifier.toLowerCase()) {
                    case "of":
                        tokens.add(new Token(TokenType.FOR_OF, identifier, line, position));
                        break;
                    case "delimiter":
                        tokens.add(new Token(TokenType.FOR_DELIMITER, identifier, line, position));
                        break;
                    case "open":
                        tokens.add(new Token(TokenType.FOR_OPEN, identifier, line, position));
                        break;
                    case "close":
                        tokens.add(new Token(TokenType.FOR_CLOSE, identifier, line, position));
                        break;
                    case "throw":
                        tokens.add(new Token(TokenType.CHECK_THROW, identifier, line, position));
                        break;
                    default:
                        tokens.add(new Token(TokenType.IDENTIFIER, identifier, line, position));
                        break;
                }
            } else if (Character.isDigit(current)) {
                String number = readWhile(c -> Character.isDigit(c) || c == '.');
                tokens.add(new Token(TokenType.NUMBER, number, line, position));
            } else {
                String identifier = readWhile(c -> !Character.isWhitespace(c));
                tokens.add(new Token(TokenType.UNKNOWN, identifier, line, position));
            }
        }
        tokens.add(new Token(TokenType.NEWLINE, "\n", line, position));
        return tokens;
    }
}
