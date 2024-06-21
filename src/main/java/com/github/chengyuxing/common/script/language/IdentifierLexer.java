package com.github.chengyuxing.common.script.language;

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
        return position < length ? input.charAt(position) : '\0';
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
            if (current == '\0') {
                break;
            } else if (current == '\n') {
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
                        tokens.add(new Token(TokenType.ENDFOR, "#done"));
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
            } else if (Character.isDigit(current)) {
                String number = readWhile(Character::isDigit);
                tokens.add(new Token(TokenType.NUMBER, number));
            } else {
                String identifier = readWhile(c -> !Character.isWhitespace(c) && c != '\n' && c != '\0');
                switch (identifier.toLowerCase()) {
                    case "of":
                        tokens.add(new Token(TokenType.OF, "of"));
                        break;
                    case "delimiter":
                        tokens.add(new Token(TokenType.DELIMITER, "delimiter"));
                        break;
                    case "open":
                        tokens.add(new Token(TokenType.OPEN, "open"));
                        break;
                    case "close":
                        tokens.add(new Token(TokenType.CLOSE, "close"));
                        break;
                    default:
                        if (identifier.startsWith(":") && !identifier.startsWith("::")) {
                            tokens.add(new Token(TokenType.NAMED_PARAMETER, identifier));
                        } else {
                            tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                        }
                        break;
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}
