package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.lang.Token;
import com.github.chengyuxing.common.script.lang.TokenType;
import com.github.chengyuxing.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Key expression parser.
 */
public class KeyExpressionParser {
    public static final Pattern SINGLE_QUOTE_STRING_PATTERN = Pattern.compile("'(?:\\\\'|[^'])*'");
    public static final Pattern DOUBLE_QUOTE_STRING_PATTERN = Pattern.compile("\"(?:\\\\\"|[^\"])*\"");
    public static final Pattern VAR_KEY_PATTERN = Pattern.compile("[\\p{L}_][\\p{L}\\p{N}_]*");
    public static final Pattern EXPRESSION_PATTERN = Pattern.compile(VAR_KEY_PATTERN + "(?:\\." + VAR_KEY_PATTERN + "|\\[(?:\\d+|" + SINGLE_QUOTE_STRING_PATTERN + "|" + DOUBLE_QUOTE_STRING_PATTERN + ")])*");
    private final List<Token> tokens;
    private int index;
    private Token currentToken;

    public KeyExpressionParser(List<Token> tokens) {
        this.tokens = tokens;
        this.index = 0;
        this.currentToken = this.tokens.get(0);
    }

    private boolean peek(TokenType type) {
        return currentToken.getType() == type;
    }

    private void advance() {
        index++;
        if (index < tokens.size()) {
            currentToken = tokens.get(index);
        } else {
            Token lastToken = tokens.get(index - 1);
            currentToken = new Token(TokenType.EOF, "", lastToken.getLine(), lastToken.getColumn());
        }
    }

    private void eat(TokenType type) {
        if (currentToken.getType() == type) {
            advance();
        } else {
            throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + type);
        }
    }

    public int getConsumedTokenIndex() {
        return index;
    }

    public List<String> parse() {
        List<String> keys = new ArrayList<>();
        keys.add(currentToken.getValue());
        eat(TokenType.IDENTIFIER);
        while (!peek(TokenType.NEWLINE) && !peek(TokenType.EOF)) {
            if (peek(TokenType.DOT)) {
                advance();
                keys.add(currentToken.getValue());
                if (peek(TokenType.IDENTIFIER)) {
                    advance();
                } else {
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER);
                }
            } else if (peek(TokenType.LBRACKET)) {
                advance();
                if (peek(TokenType.NUMBER)) {
                    if (!StringUtils.isNonNegativeInteger(currentToken.getValue())) {
                        throw new ScriptSyntaxException("Index must be a non-negative integer: " + currentToken.getValue());
                    }
                } else if (!peek(TokenType.STRING)) {
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.STRING + " / " + TokenType.NUMBER);
                }
                keys.add(currentToken.getValue());
                advance();
                eat(TokenType.RBRACKET);
            } else {
                break;
            }
        }
        return keys;
    }

    /**
     * get the expression first dot index.
     *
     * @param expression keypath e.g. {@code user.name} or {@code users[0]}
     * @return first dot index
     */
    public static int getFirstDotIndex(String expression) {
        int idx = -1;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '.') {
                idx = i;
                break;
            }
            if (expression.charAt(i) == '[') {
                idx = i;
                break;
            }
        }
        return idx;
    }
}
