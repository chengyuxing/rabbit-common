package com.github.chengyuxing.common.script.lexer;

import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Flow-Control lexer</h2>
 */
public class FlowControlLexer {
    public static final String IF = "#if";
    public static final String ELSE = "#else";
    public static final String FI = "#fi";
    public static final String CHOOSE = "#choose";
    public static final String WHEN = "#when";
    public static final String SWITCH = "#switch";
    public static final String CASE = "#case";
    public static final String DEFAULT = "#default";
    public static final String BREAK = "#break";
    public static final String END = "#end";
    public static final String FOR = "#for";
    public static final String DONE = "#done";

    public static final String[] KEYWORDS = new String[]{
            IF, ELSE, FI, CHOOSE, WHEN, SWITCH, CASE, DEFAULT, BREAK, END, FOR, DONE
    };

    private final String[] lines;
    private int position;
    private final int length;

    public FlowControlLexer(String input) {
        this.lines = input.split("\n");
        this.length = this.lines.length;
        this.position = 0;
    }

    /**
     * Trim each line for search prefix {@code #} to detect expression.
     *
     * @param line current line
     * @return expression or normal line
     * @see #IF
     */
    protected String trimExpression(String line) {
        String tl = line.trim();
        if (tl.startsWith("#")) {
            return tl;
        }
        return line;
    }

    private String currentLine() {
        return position < length ? lines[position] : "\0";
    }

    private void advance() {
        position++;
    }

    private void skipEmptyLine() {
        while (currentLine().trim().isEmpty() && !currentLine().equals("\0")) {
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
            }
            current = trimExpression(current);
            if (StringUtil.startsWithsIgnoreCase(current, KEYWORDS)) {
                IdentifierLexer lexer = new IdentifierLexer(current);
                tokens.addAll(lexer.tokenize());
            } else {
                tokens.add(new Token(TokenType.PLAIN_TEXT, current + '\n'));
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}
