package com.github.chengyuxing.common.script.lexer;

import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.github.chengyuxing.common.script.Directives.*;

/**
 * <h2>Rabbit script lexer</h2>
 */
public class RabbitScriptLexer {
    public static final String[] DIRECTIVES = new String[]{
            IF, ELSE, FI, CHOOSE, WHEN, SWITCH, CASE, DEFAULT, BREAK, END, FOR, DONE, GUARD, THROW, CHECK, VAR
    };
    public static final Pattern DIRECTIVES_PATTERN = Pattern.compile("(?i)\\s*(?:" + String.join("|", DIRECTIVES) + ")(?:\\s+.*|$)");

    private final String[] lines;
    private int position;
    private final int length;

    public RabbitScriptLexer(@NotNull String input) {
        this.lines = input.split("\n");
        this.length = this.lines.length;
        this.position = 0;
    }

    /**
     * Normalizes a given directive line by potentially trimming or adjusting it to ensure
     * it conforms to the expected format for directives.
     * <p> e.g
     * {@code [ #if :id > 0 ]} -&gt; {@code #if :id > 0}</p>
     *
     * @param line the line containing the directive to be normalized
     * @return the normalized directive line
     */
    protected String normalizeDirectiveLine(String line) {
        return line;
    }

    private String currentLine() {
        return position < length ? lines[position] : "\0";
    }

    private void advance() {
        position++;
    }

    private void skipEmptyLine() {
        while (StringUtils.isBlank(currentLine()) && !currentLine().equals("\0")) {
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
            String line = normalizeDirectiveLine(current);
            if (DIRECTIVES_PATTERN.matcher(line).matches()) {
                IdentifierLexer lexer = new IdentifierLexer(line, position);
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
