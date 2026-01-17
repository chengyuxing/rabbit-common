package com.github.chengyuxing.common.script.lexer;

import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.github.chengyuxing.common.script.Directives.*;

/**
 * <h2>Rabbit script lexer</h2>
 */
public class RabbitScriptLexer {
    public static final String[] DIRECTIVES = new String[]{
            IF, ELSE, FI, CHOOSE, WHEN, SWITCH, CASE, DEFAULT, BREAK, END, FOR, DONE, GUARD, THROW, CHECK, VAR
    };
    public static final String DIRECTIVES_PATTERN = "(?i)\\s*(?:" + String.join("|", DIRECTIVES) + ")(?:\\s+.*|$)";

    private final String[] lines;
    private int position;
    private final int length;

    public RabbitScriptLexer(@NotNull String input) {
        this.lines = input.split("\n");
        this.length = this.lines.length;
        this.position = 0;
    }

    /**
     * Trims the specified line to a valid expression by removing any unnecessary
     * leading or trailing characters.
     * <p> e.g
     * {@code [ #if :id > 0 ]} -&gt; {@code #if :id > 0}</p>
     *
     * @param line the content line to be trimmed
     * @return the trimmed expression line
     */
    protected String trimExpressionLine(String line) {
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
            if (current.equals("\0")) {
                break;
            }
            String tl = trimExpressionLine(current);
            if (tl.matches(DIRECTIVES_PATTERN)) {
                IdentifierLexer lexer = new IdentifierLexer(tl, position);
                tokens.addAll(lexer.tokenize());
            } else {
                tokens.add(new Token(TokenType.PLAIN_TEXT, current, position, 0));
            }
            advance();
        }
        tokens.add(new Token(TokenType.EOF, "", position, 0));
        return tokens;
    }
}
