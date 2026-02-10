package com.github.chengyuxing.common.script.lexer;

import com.github.chengyuxing.common.script.exception.LexerException;
import com.github.chengyuxing.common.script.lang.Token;
import com.github.chengyuxing.common.script.lang.TokenType;
import com.github.chengyuxing.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.chengyuxing.common.script.lang.Directives.*;

/**
 * <h2>Identifier Lexer</h2>
 */
public class IdentifierLexer {
    private final String input;
    private final int line;
    private int position;
    private final int length;

    public IdentifierLexer(@NotNull String input, int line) {
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
            int start = position;
            if (current == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n", line, start));
                advance();
            } else if (current == '#') {
                advance();
                String keyword = readWhile(Character::isAlphabetic);
                switch (keyword.toLowerCase()) {
                    case "if":
                        tokens.add(new Token(TokenType.IF, IF, line, start));
                        break;
                    case "else":
                        tokens.add(new Token(TokenType.ELSE, ELSE, line, start));
                        break;
                    case "fi":
                        tokens.add(new Token(TokenType.END_IF, FI, line, start));
                        break;
                    case "end":
                        tokens.add(new Token(TokenType.END, END, line, start));
                        break;
                    case "switch":
                        tokens.add(new Token(TokenType.SWITCH, SWITCH, line, start));
                        break;
                    case "case":
                        tokens.add(new Token(TokenType.CASE, CASE, line, start));
                        break;
                    case "default":
                        tokens.add(new Token(TokenType.DEFAULT, DEFAULT, line, start));
                        break;
                    case "break":
                        tokens.add(new Token(TokenType.BREAK, BREAK, line, start));
                        break;
                    case "choose":
                        tokens.add(new Token(TokenType.CHOOSE, CHOOSE, line, start));
                        break;
                    case "when":
                        tokens.add(new Token(TokenType.WHEN, WHEN, line, start));
                        break;
                    case "for":
                        tokens.add(new Token(TokenType.FOR, FOR, line, start));
                        break;
                    case "done":
                        tokens.add(new Token(TokenType.END_FOR, DONE, line, start));
                        break;
                    case "guard":
                        tokens.add(new Token(TokenType.GUARD, GUARD, line, start));
                        break;
                    case "throw":
                        tokens.add(new Token(TokenType.END_GUARD, THROW, line, start));
                        break;
                    case "check":
                        tokens.add(new Token(TokenType.CHECK, CHECK, line, start));
                        break;
                    case "var":
                        tokens.add(new Token(TokenType.DEFINE_VAR, VAR, line, start));
                        break;
                    default:
                        tokens.add(new Token(TokenType.PLAIN_TEXT, '#' + keyword, line, start));
                        break;
                }
            } else if (current == '\'') {
                advance();
                String str = readWhile(c -> {
                    if (c == '\n' || c == '\r') {
                        throw new LexerException("Unterminated string literal at: " + position);
                    }
                    return c != '\'';
                });
                if (currentChar() != '\'') {
                    throw new LexerException("Unterminated string literal at: " + position);
                }
                advance();
                tokens.add(new Token(TokenType.STRING, str, line, start));
            } else if (current == '"') {
                advance();
                String str = readWhile(c -> {
                    if (c == '\n' || c == '\r') {
                        throw new LexerException("Unterminated string literal at: " + position);
                    }
                    return c != '"';
                });
                if (currentChar() != '"') {
                    throw new LexerException("Unterminated string literal at: " + position);
                }
                advance();
                tokens.add(new Token(TokenType.STRING, str, line, start));
            } else if (current == ',') {
                tokens.add(new Token(TokenType.COMMA, ",", line, start));
                advance();
            } else if (current == '|') {
                if (match("||")) {
                    tokens.add(new Token(TokenType.LOGIC_OR, "||", line, start));
                } else {
                    tokens.add(new Token(TokenType.PIPE_SYMBOL, "|", line, start));
                    advance();
                }
            } else if (current == '&') {
                if (match("&&")) {
                    tokens.add(new Token(TokenType.LOGIC_AND, "&&", line, start));
                } else {
                    tokens.add(new Token(TokenType.AND_SYMBOL, "&", line, start));
                    advance();
                }
            } else if (current == '!') {
                if (match("!=")) {
                    tokens.add(new Token(TokenType.OPERATOR, "!=", line, start));
                } else if (match("!~")) {
                    tokens.add(new Token(TokenType.OPERATOR, "!~", line, start));
                } else if (match("!@")) {
                    tokens.add(new Token(TokenType.OPERATOR, "!@", line, start));
                } else {
                    tokens.add(new Token(TokenType.LOGIC_NOT, "!", line, start));
                    advance();
                }
            } else if (current == '=') {
                if (match("==")) {
                    tokens.add(new Token(TokenType.OPERATOR, "==", line, start));
                } else {
                    tokens.add(new Token(TokenType.OPERATOR, "=", line, start));
                    advance();
                }
            } else if (current == '>') {
                if (match(">=")) {
                    tokens.add(new Token(TokenType.OPERATOR, ">=", line, start));
                } else {
                    tokens.add(new Token(TokenType.OPERATOR, ">", line, start));
                    advance();
                }
            } else if (current == '<') {
                if (match("<=")) {
                    tokens.add(new Token(TokenType.OPERATOR, "<=", line, start));
                } else if (match("<>")) {
                    tokens.add(new Token(TokenType.OPERATOR, "<>", line, start));
                } else {
                    tokens.add(new Token(TokenType.OPERATOR, "<", line, start));
                    advance();
                }
            } else if (current == '~') {
                tokens.add(new Token(TokenType.OPERATOR, "~", line, start));
                advance();
            } else if (current == '@') {
                tokens.add(new Token(TokenType.OPERATOR, "@", line, start));
                advance();
            } else if (current == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", line, start));
                advance();
            } else if (current == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", line, start));
                advance();
            } else if (current == '{') {
                tokens.add(new Token(TokenType.LBRACE, "{", line, start));
                advance();
            } else if (current == '}') {
                tokens.add(new Token(TokenType.RBRACE, "}", line, start));
                advance();
            } else if (current == '[') {
                tokens.add(new Token(TokenType.LBRACKET, "[", line, start));
                advance();
            } else if (current == ']') {
                tokens.add(new Token(TokenType.RBRACKET, "]", line, start));
                advance();
            } else if (current == '.') {
                tokens.add(new Token(TokenType.DOT, ".", line, start));
                advance();
            } else if (current == ':') {
                tokens.add(new Token(TokenType.COLON, ":", line, start));
                advance();
            } else if (current == ';') {
                tokens.add(new Token(TokenType.SEMICOLON, ";", line, start));
                advance();
            } else if (current == '+') {
                tokens.add(new Token(TokenType.ADD_SYMBOL, "+", line, start));
                advance();
            } else if (current == '-') {
                tokens.add(new Token(TokenType.SUB_SYMBOL, "-", line, start));
                advance();
            } else if (Character.isAlphabetic(current) || current == '_') {
                String identifier = readWhile(c -> Character.isAlphabetic(c) || Character.isDigit(c) || c == '_');
                switch (identifier.toLowerCase()) {
                    case "of":
                        tokens.add(new Token(TokenType.FOR_OF, identifier, line, start));
                        break;
                    case "as":
                        tokens.add(new Token(TokenType.FOR_PROPERTY_AS, identifier, line, start));
                        break;
                    case "delimiter":
                        tokens.add(new Token(TokenType.FOR_DELIMITER, identifier, line, start));
                        break;
                    case "open":
                        tokens.add(new Token(TokenType.FOR_OPEN, identifier, line, start));
                        break;
                    case "close":
                        tokens.add(new Token(TokenType.FOR_CLOSE, identifier, line, start));
                        break;
                    case "throw":
                        tokens.add(new Token(TokenType.CHECK_THROW, identifier, line, start));
                        break;
                    default:
                        tokens.add(new Token(TokenType.IDENTIFIER, identifier, line, start));
                        break;
                }
            } else if (StringUtils.isAsciiDigit(current)) {
                String number = readWhile(StringUtils::isAsciiDigit);
                tokens.add(new Token(TokenType.NUMBER, number, line, start));
            } else {
                String str = readWhile(c -> !Character.isWhitespace(c));
                tokens.add(new Token(TokenType.PLAIN_TEXT, str, line, start));
            }
        }
        tokens.add(new Token(TokenType.NEWLINE, "\n", line, position));
        return tokens;
    }
}
