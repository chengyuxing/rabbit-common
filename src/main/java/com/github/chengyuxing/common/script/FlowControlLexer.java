package com.github.chengyuxing.common.script;

import java.util.ArrayList;
import java.util.List;

public class FlowControlLexer {
    private final String[] lines;
    private int position;
    private final int length;

    public FlowControlLexer(String input) {
        this.lines = input.split("\n");
        this.length = this.lines.length;
        this.position = 0;
    }

    private String currentLine() {
        return position < length ? lines[position] : "\0";
    }

    private void advance() {
        position++;
    }

    private void skipEmptyLine() {
        while (currentLine().trim().isEmpty()) {
            advance();
        }
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (position < length) {
            skipEmptyLine();
            String current = currentLine();
            advance();
            if (current.equals("\0")) {
                break;
            } else if (current.trim().startsWith("#")) {
                IdentifierLexer lexer = new IdentifierLexer(current);
                tokens.addAll(lexer.tokenize());
            } else {
                tokens.add(new Token(TokenType.PLAIN_TEXT, current));
            }
            tokens.add(new Token(TokenType.NEWLINE, "\n"));
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}
