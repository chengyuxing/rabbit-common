package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.expression.Patterns;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class IdentifierLexer {
    private final String input;
    private int position;
    private final int length;

    public IdentifierLexer(String input) {
        this.input = input;
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
        while (predicate.test(currentChar())) {
            sb.append(currentChar());
            advance();
        }
        return sb.toString();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (position < length) {
            skipWhitespace();
            char current = currentChar();
            if (current == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\n"));
                advance();
            } else if (current == '#') {
                advance();
                String keyword = readWhile(Character::isAlphabetic);
                switch (keyword.toLowerCase()) {
                    case "if":
                        tokens.add(new Token(TokenType.IF, "#if"));
                        break;
                    case "else":
                        tokens.add(new Token(TokenType.ELSE, "#else"));
                        break;
                    case "fi":
                        tokens.add(new Token(TokenType.ENDIF, "#fi"));
                        break;
                    case "end":
                        tokens.add(new Token(TokenType.END, "#end"));
                        break;
                    case "switch":
                        tokens.add(new Token(TokenType.SWITCH, "#switch"));
                        break;
                    case "case":
                        tokens.add(new Token(TokenType.CASE, "#case"));
                        break;
                    case "default":
                        tokens.add(new Token(TokenType.DEFAULT, "#default"));
                        break;
                    case "break":
                        tokens.add(new Token(TokenType.BREAK, "#break"));
                        break;
                    case "choose":
                        tokens.add(new Token(TokenType.CHOOSE, "#choose"));
                        break;
                    case "when":
                        tokens.add(new Token(TokenType.WHEN, "#when"));
                        break;
                    case "for":
                        tokens.add(new Token(TokenType.FOR, "#for"));
                        break;
                    case "done":
                        tokens.add(new Token(TokenType.END_FOR, "#done"));
                        break;
                    default:
                        tokens.add(new Token(TokenType.UNKNOWN, '#' + keyword));
                        break;
                }
            } else if (current == '\'') {
                advance();
                String str = readWhile(c -> c != '\'');
                advance();
                tokens.add(new Token(TokenType.STRING, str));
            } else {
                String identifier = readWhile(c -> !Character.isWhitespace(c) && c != '\n');
                switch (identifier.toLowerCase()) {
                    case "of":
                        tokens.add(new Token(TokenType.FOR_OF, "of"));
                        break;
                    case "delimiter":
                        tokens.add(new Token(TokenType.FOR_DELIMITER, "delimiter"));
                        break;
                    case "open":
                        tokens.add(new Token(TokenType.FOR_OPEN, "open"));
                        break;
                    case "close":
                        tokens.add(new Token(TokenType.FOR_CLOSE, "close"));
                        break;
                    default:
                        if (identifier.matches(":" + Patterns.VAR_KEY_PATTERN)) {
                            tokens.add(new Token(TokenType.VARIABLE_NAME, identifier));
                        } else if (StringUtil.isNumeric(identifier)) {
                            tokens.add(new Token(TokenType.NUMBER, identifier));
                        } else {
                            tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                        }
                        break;
                }
            }
        }
        return tokens;
    }
}
