package com.github.chengyuxing.common.script;

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
                        tokens.add(new Token(TokenType.IF, FlowControlLexer.IF));
                        break;
                    case "else":
                        tokens.add(new Token(TokenType.ELSE, FlowControlLexer.ELSE));
                        break;
                    case "fi":
                        tokens.add(new Token(TokenType.ENDIF, FlowControlLexer.FI));
                        break;
                    case "end":
                        tokens.add(new Token(TokenType.END, FlowControlLexer.END));
                        break;
                    case "switch":
                        tokens.add(new Token(TokenType.SWITCH, FlowControlLexer.SWITCH));
                        break;
                    case "case":
                        tokens.add(new Token(TokenType.CASE, FlowControlLexer.CASE));
                        break;
                    case "default":
                        tokens.add(new Token(TokenType.DEFAULT, FlowControlLexer.DEFAULT));
                        break;
                    case "break":
                        tokens.add(new Token(TokenType.BREAK, FlowControlLexer.BREAK));
                        break;
                    case "choose":
                        tokens.add(new Token(TokenType.CHOOSE, FlowControlLexer.CHOOSE));
                        break;
                    case "when":
                        tokens.add(new Token(TokenType.WHEN, FlowControlLexer.WHEN));
                        break;
                    case "for":
                        tokens.add(new Token(TokenType.FOR, FlowControlLexer.FOR));
                        break;
                    case "done":
                        tokens.add(new Token(TokenType.END_FOR, FlowControlLexer.DONE));
                        break;
                    default:
                        tokens.add(new Token(TokenType.UNKNOWN, '#' + keyword));
                        break;
                }
            } else if (current == '\'') {
                advance();
                String str = readWhile(c -> c != '\'');
                advance();
                tokens.add(new Token(TokenType.STRING, '\'' + str + '\''));
            } else if (current == '"') {
                advance();
                String str = readWhile(c -> c != '"');
                advance();
                tokens.add(new Token(TokenType.STRING, '"' + str + '"'));
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
                        if (identifier.charAt(0) == ':') {
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
        tokens.add(new Token(TokenType.NEWLINE, "\n"));
        return tokens;
    }
}
