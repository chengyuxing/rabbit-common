package com.github.chengyuxing.common.script.parser;

import com.github.chengyuxing.common.CleanStringJoiner;
import com.github.chengyuxing.common.script.Directives;
import com.github.chengyuxing.common.script.exception.CheckViolationException;
import com.github.chengyuxing.common.script.exception.GuardViolationException;
import com.github.chengyuxing.common.script.exception.PipeNotFoundException;
import com.github.chengyuxing.common.script.pipe.BuiltinPipes;
import com.github.chengyuxing.common.script.pipe.IPipe;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.common.util.ValueUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;

import static com.github.chengyuxing.common.util.StringUtils.NEW_LINE;

/**
 * <h2>Rabbit script engine.</h2>
 * <p>check statement:</p>
 * <blockquote>
 * <pre>
 * #check expression throw 'message'
 * </pre>
 * </blockquote>
 * <p>var statement:</p>
 * <blockquote>
 * <pre>
 * #var myVal = :key [| {@linkplain IPipe pipe1} | {@linkplain IPipe pipeN} | ...]
 * </pre>
 * </blockquote>
 * <p>if statement:</p>
 * <blockquote>
 * <pre>
 * #if <i>expression1</i>
 *      #if <i>expression2</i>
 *      ...
 *      #fi
 *      #if <i>expression3</i>
 *      ...
 *      #else
 *      ...
 *      #fi
 * #fi
 * </pre>
 * </blockquote>
 * <p>guard statement:</p>
 * <blockquote>
 * <pre>
 * #guard <i>expression</i>
 *     ...
 * #throw 'message'
 * </pre>
 * </blockquote>
 * <p>choose statement:</p>
 * <blockquote>
 * <pre>
 * #choose
 *      #when <i>expression1</i>
 *      ...
 *      #break
 *      #when <i>expression2</i>
 *      ...
 *      #break
 *      ...
 *      #default
 *      ...
 *      #break
 * #end
 * </pre>
 * </blockquote>
 * <p>switch statement</p>
 * <blockquote>
 * <pre>
 * #switch :key [| {@linkplain IPipe pipe1} | {@linkplain IPipe pipeN} | ...]
 *      #case var1[, var2][, varN],...
 *      ...
 *      #break
 *      #case var3
 *      ...
 *      #break
 *      ...
 *      #default
 *      ...
 *      #break
 * #end
 * </pre>
 * </blockquote>
 * <p>for statement</p>
 * <blockquote>
 * <pre>
 * #for item[,idx] of :list [| {@linkplain IPipe pipe1} | pipeN | ... ] [delimiter ','] [open ''] [close '']
 *     ...
 * #done
 * </pre>
 * </blockquote>
 * <p>Boolean condition expression.</p>
 * <p>Support logic operator: {@code &&, ||, !}, e.g.</p>
 * <blockquote><pre>!(:id &gt;= 0 || :name | {@link com.github.chengyuxing.common.script.pipe.builtin.Nvl nvl('guest')} | {@link com.github.chengyuxing.common.script.pipe.builtin.Length length} &lt;= 3) &amp;&amp; :age &gt; 21
 * </pre></blockquote>
 * Built-in {@link IPipe pipes}：{@link com.github.chengyuxing.common.script.pipe.BuiltinPipes}
 *
 * @see Comparators
 */
public class RabbitScriptEngine {
    private static final Map<String, IPipe<?>> builtinPipes = BuiltinPipes.getAll();
    private Map<String, IPipe<?>> pipes = new HashMap<>();

    private final List<Token> tokens;

    private int forIndex = 0;
    private Map<String, Object> forGeneratedVars = new HashMap<>();
    private Map<String, Object> definedVars = new HashMap<>();

    /**
     * Construct a new RabbitScriptEngine with input content.
     *
     * @param input content with flow-control scripts
     */
    public RabbitScriptEngine(String input) {
        RabbitScriptLexer lexer = new RabbitScriptLexer(input) {
            @Override
            protected String normalizeDirectiveLine(String line) {
                return RabbitScriptEngine.this.normalizeDirectiveLine(line);
            }
        };
        this.tokens = lexer.tokenize();
    }

    /**
     * Evaluate content with scripts.
     *
     * @param context context params
     * @return parsed content
     */
    public String evaluate(Map<String, Object> context) {
        if (tokens.isEmpty()) {
            return "";
        }
        forIndex = 0;
        forGeneratedVars = new HashMap<>();
        definedVars = new HashMap<>();
        Interpreter interpreter = new Interpreter(tokens, context);
        return interpreter.doParse();
    }

    /**
     * Verify scripts syntax.
     */
    public void verify() {
        if (tokens.isEmpty()) {
            return;
        }
        Verifier verifier = new Verifier(tokens);
        verifier.doVerify();
    }

    /**
     * Set custom pipes.
     *
     * @param pipes pipes map
     */
    public void setPipes(Map<String, IPipe<?>> pipes) {
        if (pipes == null) {
            return;
        }
        if (this.pipes.equals(pipes)) {
            return;
        }
        this.pipes = new HashMap<>(pipes);
    }

    /**
     * Get custom pipes.
     *
     * @return custom pipes
     */
    protected Map<String, IPipe<?>> getPipes() {
        return pipes;
    }

    /**
     * Normalizes a directive line prefix {@code #} to detect expression.
     *
     * @param line the line to be normalized
     * @return the normalized line
     */
    protected String normalizeDirectiveLine(String line) {
        return line;
    }

    /**
     * <code>#for</code> loop body content formatter, format custom template variable and args resolve, e.g.
     * <p>args:</p>
     * <blockquote>
     * <pre>
     * {
     *   users: [
     *     {name: 'cyx', name: 'json'}
     *   ]
     * }
     * </pre>
     * </blockquote>
     * <p>for expression:</p>
     * <blockquote>
     * <pre>
     * #for user,idx of :users delimiter ' and '
     *    '${user.name}'
     * #done
     * </pre>
     * </blockquote>
     * <p>result:</p>
     * <blockquote>
     * <pre>'cyx' and 'json'</pre>
     * </blockquote>
     *
     * @param forIndex  each for loop auto index
     * @param itemIndex for each item auto index
     * @param body      content in for loop
     * @param context   each for loop context args which created by for expression
     * @return formatted content
     * @see #getForGeneratedVars()
     */
    protected String forLoopBodyFormatter(int forIndex, int itemIndex, @NotNull String body, @NotNull Map<String, Object> context) {
        return body;
    }

    /**
     * Build {@code #for} var key.
     *
     * @param name   for context var name
     * @param forIdx for auto index
     * @param varIdx var auto index
     * @return unique for var key
     */
    protected String forVarGeneratedKey(String name, int forIdx, int varIdx) {
        return name + "_" + forIdx + "_" + varIdx;
    }

    /**
     * Get {@code #for} context variable map which saved by expression calc.<br>
     * Format: {@code (varName_forAutoIdx_varAutoIdx: var)}, e.g.
     * <blockquote>
     * <pre>
     * list: ["a", "b", "c"]; forIdx: 0
     * </pre>
     * <pre>
     * #for item of :list
     *      ...
     * #done
     * </pre>
     * <pre>
     * vars: {item_0_0: "a", item_0_1: "b", item_0_2: "c"}
     * </pre>
     * </blockquote>
     *
     * @return {@code #for} context variable map
     * @see #forVarGeneratedKey(String, int, int)
     */
    public @NotNull @Unmodifiable Map<String, Object> getForGeneratedVars() {
        return Collections.unmodifiableMap(forGeneratedVars);
    }

    /**
     * Returns the #var variables.
     *
     * @return vars
     */
    public @NotNull @Unmodifiable Map<String, Object> getDefinedVars() {
        return Collections.unmodifiableMap(definedVars);
    }

    /**
     * Is current token not the end token type.
     *
     * @param token     current token
     * @param endTokens end token types
     * @return true or false
     */
    private static boolean nonEndToken(Token token, TokenType... endTokens) {
        if (token.getType() == TokenType.EOF) {
            return false;
        }
        for (TokenType end : endTokens) {
            if (token.getType() == end) {
                return false;
            }
        }
        return true;
    }

    /**
     * Interpreter implementation.
     */
    public final class Interpreter {
        private final List<Token> tokens;
        private int currentTokenIndex;
        private Token currentToken;
        private final Map<String, Object> context;

        public Interpreter(List<Token> tokens, Map<String, Object> context) {
            this.tokens = tokens;
            this.context = context;
            this.currentTokenIndex = 0;
            this.currentToken = tokens.get(currentTokenIndex);
        }

        private void advance() {
            currentTokenIndex++;
            if (currentTokenIndex < tokens.size()) {
                currentToken = tokens.get(currentTokenIndex);
            } else {
                Token lastToken = tokens.get(currentTokenIndex - 1);
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

        private boolean evaluateCondition() {
            return evaluateOr();
        }

        private boolean evaluateOr() {
            boolean result = evaluateAnd();
            while (currentToken.getType() == TokenType.LOGIC_OR) {
                advance();
                boolean next = evaluateAnd();
                result = result || next;
            }
            return result;
        }

        private boolean evaluateAnd() {
            boolean result = evaluateCompare();
            while (currentToken.getType() == TokenType.LOGIC_AND) {
                advance();
                boolean next = evaluateCompare();
                result = result && next;
            }
            return result;
        }

        private boolean evaluateCompare() {
            if (currentToken.getType() == TokenType.LPAREN) {
                advance();
                boolean result = evaluateCondition();
                eat(TokenType.RPAREN);
                return result;
            }

            if (currentToken.getType() == TokenType.LOGIC_NOT) {
                advance();
                return !evaluateCompare();
            }

            Object a = extractValueWithPipes();

            String operator = currentToken.getValue();
            eat(TokenType.OPERATOR);

            Object b = extractValueWithPipes();

            return Comparators.compare(a, operator, b);
        }

        private List<Pair<String, List<Object>>> collectPipes() {
            List<Pair<String, List<Object>>> pipes = new ArrayList<>();
            while (nonEndToken(currentToken, TokenType.NEWLINE)) {
                if (currentToken.getType() == TokenType.PIPE_SYMBOL) {
                    advance();
                    String pipeName = currentToken.getValue();
                    eat(TokenType.IDENTIFIER);
                    List<Object> params = collectPipeParams();
                    pipes.add(Pair.of(pipeName, params));
                } else {
                    break;
                }
            }
            return pipes;
        }

        private List<Object> collectPipeParams() {
            List<Object> params = new ArrayList<>();
            if (currentToken.getType() == TokenType.LPAREN) {
                advance();
                while (nonEndToken(currentToken, TokenType.RPAREN, TokenType.NEWLINE)) {
                    params.add(extractValue());
                    if (currentToken.getType() == TokenType.COMMA) {
                        advance();
                        if (currentToken.getType() == TokenType.RPAREN) {
                            throw new ScriptSyntaxException("Illegal token: " + TokenType.COMMA + " before " + currentToken);
                        }
                    } else {
                        break;
                    }
                }
                eat(TokenType.RPAREN);
            }
            return params;
        }

        /**
         * Returns the piped value.
         *
         * @param value value
         * @return boxed value
         */
        private Object calcPipedValue(Object value) {
            Object res = value;
            for (Pair<String, List<Object>> pipe : collectPipes()) {
                String pipeName = pipe.getItem1();
                List<Object> pipeParams = pipe.getItem2();
                if (getPipes().containsKey(pipeName)) {
                    res = getPipes().get(pipeName).transform(res, pipeParams.toArray());
                } else if (builtinPipes.containsKey(pipeName)) {
                    res = builtinPipes.get(pipeName).transform(res, pipeParams.toArray());
                } else {
                    throw new PipeNotFoundException("Cannot find pipe '" + pipe + "', near " + currentToken);
                }
            }
            return res;
        }

        /**
         * Extract variable keys by provided key path expression, e.g. {@code user.addresses[0]}
         *
         * @return keys list
         */
        private List<String> extractVariableKeys() {
            List<String> paths = new ArrayList<>();
            paths.add(currentToken.getValue());
            eat(TokenType.IDENTIFIER);
            while (nonEndToken(currentToken, TokenType.NEWLINE)) {
                if (currentToken.getType() == TokenType.DOT) {
                    advance();
                    paths.add(currentToken.getValue());
                    if (currentToken.getType() == TokenType.IDENTIFIER || currentToken.getType() == TokenType.NUMBER) {
                        advance();
                    } else {
                        throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + " / " + TokenType.NUMBER);
                    }
                } else if (currentToken.getType() == TokenType.LBRACKET) {
                    advance();
                    if (!StringUtils.isNonNegativeInteger(currentToken.getValue())) {
                        throw new ScriptSyntaxException("Index must be a non-negative integer: " + currentToken.getValue());
                    }
                    paths.add(currentToken.getValue());
                    advance();
                    eat(TokenType.RBRACKET);
                } else {
                    break;
                }
            }
            return paths;
        }

        private Object parseNumberLiteralValue(String sign) {
            String number = sign + currentToken.getValue();
            eat(TokenType.NUMBER);
            if (currentToken.getType() == TokenType.DOT) {
                number += ".";
                advance();
                number += currentToken.getValue();
                eat(TokenType.NUMBER);
            }
            return new BigDecimal(number);
        }

        private Object parseIdentifierLiteralValue() {
            String literal = currentToken.getValue();
            Object value;
            switch (literal.toLowerCase()) {
                case "null":
                    value = null;
                    break;
                case "blank":
                    value = "";
                    break;
                case "true":
                case "false":
                    value = Boolean.parseBoolean(literal);
                    break;
                default:
                    // other identifier tokens as string type e.g: a1, user_id
                    value = literal;
                    break;
            }
            advance();
            return value;
        }

        private Object extractValueWithPipes() {
            Object value = extractValue();
            return calcPipedValue(value);
        }

        private Object extractValue() {
            String literal = currentToken.getValue();
            Object value;
            switch (currentToken.getType()) {
                case IDENTIFIER:
                    value = parseIdentifierLiteralValue();
                    break;
                case STRING:
                case UNKNOWN:
                    value = literal;
                    advance();
                    break;
                case NUMBER:
                    value = parseNumberLiteralValue("+");
                    break;
                case ADD_SYMBOL:
                case SUB_SYMBOL:
                    String sign = currentToken.getValue();
                    advance();
                    switch (currentToken.getType()) {
                        case NUMBER:
                            value = parseNumberLiteralValue(sign);
                            break;
                        case IDENTIFIER:
                        case STRING:
                        case UNKNOWN:
                            value = sign + literal;
                            advance();
                            break;
                        default:
                            throw new ScriptSyntaxException("Symbol '" + sign + "' cannot at the start of " + currentToken);
                    }
                    break;
                case COLON:
                    advance();
                    List<String> keys = extractVariableKeys();
                    if (definedVars.containsKey(keys.get(0))) {
                        value = ValueUtils.accessDeepValue(definedVars, keys);
                    } else {
                        value = ValueUtils.accessDeepValue(context, keys);
                    }
                    break;
                default:
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + " / " + TokenType.STRING + " / " + TokenType.NUMBER + " / :<variable> e.g. user, user.name, books[0]");
            }
            return value;
        }

        /**
         * For switch's case values.
         *
         * @return multiple case values
         */
        private List<Object> collectCaseValues() {
            List<Object> values = new ArrayList<>();
            values.add(extractValueWithPipes());
            while (nonEndToken(currentToken, TokenType.NEWLINE)) {
                eat(TokenType.COMMA);
                values.add(extractValueWithPipes());
            }
            return values;
        }

        private List<Token> collectForBlock() {
            List<Token> tokens = new ArrayList<>();
            int forDepth = 0;
            while ((currentToken.getType() != TokenType.END_FOR || forDepth != 0) && nonEndToken(currentToken)) {
                if (currentToken.getType() == TokenType.FOR) {
                    forDepth++;
                } else if (currentToken.getType() == TokenType.END_FOR) {
                    forDepth--;
                }
                tokens.add(currentToken);
                advance();
            }
            return tokens;
        }

        private List<Token> collectBranchBlock() {
            List<Token> caseWhenDefaultBlock = new ArrayList<>();
            int switchChooseDepth = 0;
            while ((currentToken.getType() != TokenType.BREAK || switchChooseDepth != 0) && nonEndToken(currentToken)) {
                if (currentToken.getType() == TokenType.CHOOSE || currentToken.getType() == TokenType.SWITCH) {
                    switchChooseDepth++;
                } else if (currentToken.getType() == TokenType.END) {
                    switchChooseDepth--;
                }
                caseWhenDefaultBlock.add(currentToken);
                advance();
            }
            return caseWhenDefaultBlock;
        }

        private String parseIfStatement() {
            eat(TokenType.IF);
            boolean matched = evaluateCondition();
            eat(TokenType.NEWLINE);
            List<Token> ifBlockContent = new ArrayList<>();
            int ifDepth = 0;
            int elseIndex = -1;
            int index = 0;
            while ((currentToken.getType() != TokenType.END_IF || ifDepth != 0) && nonEndToken(currentToken)) {
                if (currentToken.getType() == TokenType.IF) {
                    ifDepth++;
                } else if (currentToken.getType() == TokenType.END_IF) {
                    ifDepth--;
                } else if (currentToken.getType() == TokenType.ELSE && ifDepth == 0) {
                    elseIndex = index;
                }
                ifBlockContent.add(currentToken);
                index++;
                advance();
            }
            eat(TokenType.END_IF);
            eat(TokenType.NEWLINE);

            List<Token> thenContent;
            List<Token> elseContent = new ArrayList<>();
            if (elseIndex == -1) {
                thenContent = ifBlockContent;
            } else {
                thenContent = ifBlockContent.subList(0, elseIndex);
                elseContent = ifBlockContent.subList(elseIndex + 1, ifBlockContent.size());
            }

            List<Token> matchedContent = matched ? thenContent : elseContent;
            if (matchedContent.isEmpty()) {
                return "";
            }
            Interpreter interpreter = new Interpreter(matchedContent, context);
            return interpreter.doParse();
        }

        private String parseGuardStatement() {
            eat(TokenType.GUARD);
            boolean matched = evaluateCondition();
            eat(TokenType.NEWLINE);
            List<Token> matchedContent = new ArrayList<>();
            int guardDepth = 0;
            while ((currentToken.getType() != TokenType.END_GUARD || guardDepth != 0) && nonEndToken(currentToken)) {
                if (currentToken.getType() == TokenType.GUARD) {
                    guardDepth++;
                } else if (currentToken.getType() == TokenType.END_GUARD) {
                    guardDepth--;
                }
                matchedContent.add(currentToken);
                advance();
            }
            eat(TokenType.END_GUARD);
            if (currentToken.getType() != TokenType.STRING && currentToken.getType() != TokenType.NEWLINE) {
                throw new ScriptSyntaxException("Illegal token: " + currentToken + ", excepted: " + TokenType.STRING + " / " + TokenType.NEWLINE);
            }
            String message = "#throw 'the reason of failure'";
            if (currentToken.getType() == TokenType.STRING) {
                message = currentToken.getValue();
                advance();
            }
            eat(TokenType.NEWLINE);

            if (!matched) {
                throw new GuardViolationException(message);
            }
            if (matchedContent.isEmpty()) {
                return "";
            }
            Interpreter interpreter = new Interpreter(matchedContent, context);
            return interpreter.doParse();
        }

        private void parseCheckStatement() {
            eat(TokenType.CHECK);
            boolean matched = evaluateCondition();
            eat(TokenType.CHECK_THROW);
            if (currentToken.getType() != TokenType.STRING) {
                throw new ScriptSyntaxException("Unexcepted token: " + currentToken + ", excepted: " + TokenType.STRING);
            }
            String message = currentToken.getValue();
            advance();
            eat(TokenType.NEWLINE);
            if (matched) {
                throw new CheckViolationException(message);
            }
        }

        /**
         * #var id = :id | upper
         */
        private void parseVarStatement() {
            parseVarStatement((varName, value) -> {
                if (context.containsKey(varName)) {
                    throw new IllegalArgumentException("Duplicate variable name: " + varName);
                }
                if (definedVars.containsKey(varName)) {
                    throw new IllegalArgumentException("Variable already defined: " + varName);
                }
                definedVars.put(varName, value);
            });
        }

        private void parseVarStatement(BiConsumer<String, Object> varConsumer) {
            eat(TokenType.DEFINE_VAR);
            String varName = currentToken.getValue();
            eat(TokenType.IDENTIFIER);
            if (currentToken.getType() == TokenType.OPERATOR && currentToken.getValue().equals("=")) {
                advance();
                Object value = extractValueWithPipes();
                eat(TokenType.NEWLINE);
                varConsumer.accept(varName, value);
            } else {
                throw new ScriptSyntaxException("Unexcepted token: " + currentToken + ", excepted: '=' operator");
            }
        }

        private String parseSwitchStatement() {
            eat(TokenType.SWITCH);
            Object variableValue = extractValueWithPipes();
            eat(TokenType.NEWLINE);

            List<Token> matchedBranch = null;
            List<Token> defaultBranch = new ArrayList<>();

            while (nonEndToken(currentToken, TokenType.END)) {
                if (currentToken.getType() == TokenType.CASE) {
                    advance();
                    List<Object> caseValues = collectCaseValues();
                    eat(TokenType.NEWLINE);
                    List<Token> caseContent = collectBranchBlock();
                    eat(TokenType.BREAK);

                    if (matchedBranch == null) {
                        for (Object caseValue : caseValues) {
                            if (Comparators.compare(variableValue, "=", caseValue)) {
                                matchedBranch = caseContent;
                                break;
                            }
                        }
                    }
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    defaultBranch = collectBranchBlock();
                    eat(TokenType.BREAK);
                } else {
                    eat(TokenType.NEWLINE);
                }
            }
            eat(TokenType.END);
            eat(TokenType.NEWLINE);
            return parseMatchedBranch(matchedBranch, defaultBranch);
        }

        private String parseChooseStatement() {
            eat(TokenType.CHOOSE);
            eat(TokenType.NEWLINE);

            List<Token> matchedBranch = null;
            List<Token> defaultBranch = new ArrayList<>();

            while (nonEndToken(currentToken, TokenType.END)) {
                if (currentToken.getType() == TokenType.WHEN) {
                    advance();
                    boolean matched = evaluateCondition();
                    eat(TokenType.NEWLINE);
                    List<Token> whenContent = collectBranchBlock();
                    eat(TokenType.BREAK);

                    if (matched && matchedBranch == null) {
                        matchedBranch = whenContent;
                    }
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    defaultBranch = collectBranchBlock();
                    eat(TokenType.BREAK);
                } else {
                    eat(TokenType.NEWLINE);
                }
            }
            eat(TokenType.END);
            eat(TokenType.NEWLINE);
            return parseMatchedBranch(matchedBranch, defaultBranch);
        }

        private String parseMatchedBranch(List<Token> matchedBranch, List<Token> defaultBranch) {
            matchedBranch = matchedBranch == null ? defaultBranch : matchedBranch;

            if (matchedBranch.isEmpty()) {
                return "";
            }
            Interpreter interpreter = new Interpreter(matchedBranch, context);
            return interpreter.doParse();
        }

        private String parseForStatement() {
            Token forToken = currentToken;
            eat(TokenType.FOR);
            String itemName = currentToken.getValue();
            eat(TokenType.IDENTIFIER);
            String idxName = "";
            if (currentToken.getType() == TokenType.COMMA) {
                advance();
                idxName = currentToken.getValue();
                eat(TokenType.IDENTIFIER);
                if (itemName.equals(idxName)) {
                    throw new ScriptSyntaxException("#for statement item and index must not have the same name: '" + itemName + "', near " + currentToken);
                }
            }
            eat(TokenType.FOR_OF);

            Object obj = extractValueWithPipes();

            String delimiter = ", ";
            String open = "";
            String close = "";
            if (currentToken.getType() == TokenType.FOR_DELIMITER) {
                advance();
                delimiter = currentToken.getValue();
                eat(TokenType.STRING);
            }
            if (currentToken.getType() == TokenType.FOR_OPEN) {
                advance();
                open = currentToken.getValue();
                eat(TokenType.STRING);
            }
            if (currentToken.getType() == TokenType.FOR_CLOSE) {
                advance();
                close = currentToken.getValue();
                eat(TokenType.STRING);
            }

            eat(TokenType.NEWLINE);
            List<Token> forContent = collectForBlock();
            eat(TokenType.END_FOR);
            eat(TokenType.NEWLINE);

            if (forContent.isEmpty()) {
                return "";
            }

            if (!open.isEmpty()) {
                open = open + NEW_LINE;
            }
            if (!close.isEmpty()) {
                close = NEW_LINE + close;
            }

            CleanStringJoiner result = new CleanStringJoiner(delimiter + NEW_LINE);
            int i = 0;
            for (Object item : ValueUtils.asIterable(obj)) {
                Map<String, Object> eachLoopVars = new HashMap<>();

                if (!itemName.isEmpty()) {
                    if (context.containsKey(itemName)) {
                        throw new IllegalArgumentException("Item name has already been used in the context '" + itemName + "' of " + forToken);
                    }
                    forGeneratedVars.put(forVarGeneratedKey(itemName, forIndex, i), item);
                    eachLoopVars.put(itemName, item);
                }
                if (!idxName.isEmpty()) {
                    if (context.containsKey(idxName)) {
                        throw new IllegalArgumentException("Index name has already been used in the context '" + idxName + "' of " + forToken);
                    }
                    forGeneratedVars.put(forVarGeneratedKey(idxName, forIndex, i), i);
                    eachLoopVars.put(idxName, i);
                }

                // for loop body content tokens.
                List<Token> newForContent = new ArrayList<>(forContent.size());
                int forPosition = forContent.indexOf(new Token(TokenType.FOR, Directives.FOR));
                for (int k = 0; k < forContent.size(); k++) {
                    Token token = forContent.get(k);
                    // only parsing #var in current layer in nest for loop
                    if (token.getType() == TokenType.DEFINE_VAR && (forPosition == -1 || forPosition > k)) {
                        List<Token> startVarTokens = forContent.subList(k, forContent.size());
                        int endVarPosition = startVarTokens.indexOf(new Token(TokenType.NEWLINE, NEW_LINE));
                        List<Token> varTokens = startVarTokens.subList(0, endVarPosition + 1);

                        final int finalI = i;
                        // the #var value probably need input context e.g. #var a = :id
                        Map<String, Object> eachLoopContext = new HashMap<>(context);
                        eachLoopContext.putAll(eachLoopVars);
                        Interpreter interpreter = new Interpreter(varTokens, eachLoopContext);
                        interpreter.parseVarStatement((varName, value) -> {
                            // def vars in the #for, then as the #for context vars
                            if (context.containsKey(varName)) {
                                throw new IllegalArgumentException("Variable already defined in the context '" + varName + "' of " + token);
                            }
                            if (eachLoopVars.containsKey(varName)) {
                                throw new IllegalArgumentException("Variable already defined in the current for loop context '" + varName + "' of " + token);
                            }
                            forGeneratedVars.put(forVarGeneratedKey(varName, forIndex, finalI), value);
                            eachLoopVars.put(varName, value);
                        });
                        // skip the current #var expression all tokens of line
                        k += endVarPosition;
                        continue;
                    }
                    if (token.getType() == TokenType.PLAIN_TEXT) {
                        String old = token.getValue();
                        // plain text format only need the vars generated by current for loop
                        // other input vars will be used in the final step e.g. FMT.format(...)
                        String newValue = forLoopBodyFormatter(forIndex, i, old, eachLoopVars);
                        Token newToken = new Token(token.getType(), newValue, token.getLine(), token.getColumn());
                        newForContent.add(newToken);
                        continue;
                    }
                    newForContent.add(token);
                }
                // the next interpreter is a new complete start
                // the content of #for block always need the input context
                Map<String, Object> eachLoopContext = new HashMap<>(context);
                eachLoopContext.putAll(eachLoopVars);
                Interpreter interpreter = new Interpreter(newForContent, eachLoopContext);
                String forContentResult = interpreter.doParse();
                result.add(forContentResult);
                i++;
            }
            forIndex++;
            String resultFor = result.toString();
            if (StringUtils.isBlank(resultFor)) {
                return "";
            }
            return open + resultFor + close;
        }

        /**
         * Parse tokens.
         *
         * @return parsed tokens
         */
        public String doParse() {
            if (tokens.isEmpty()) {
                return "";
            }
            CleanStringJoiner result = new CleanStringJoiner(NEW_LINE);
            while (nonEndToken(currentToken)) {
                switch (currentToken.getType()) {
                    case IF:
                        result.add(parseIfStatement());
                        break;
                    case SWITCH:
                        result.add(parseSwitchStatement());
                        break;
                    case CHOOSE:
                        result.add(parseChooseStatement());
                        break;
                    case FOR:
                        result.add(parseForStatement());
                        break;
                    case GUARD:
                        result.add(parseGuardStatement());
                        break;
                    case CHECK:
                        parseCheckStatement();
                        break;
                    case DEFINE_VAR:
                        parseVarStatement();
                        break;
                    case END_IF:
                    case ELSE:
                    case END:
                    case END_FOR:
                    case WHEN:
                    case CASE:
                    case DEFAULT:
                    case BREAK:
                    case END_GUARD:
                        throw new ScriptSyntaxException("Unexpected " + currentToken + " statement without preceding matching statement");
                    default:
                        result.add(currentToken.getValue());
                        advance();
                        break;
                }
            }
            return result.toString();
        }
    }

    /**
     * Syntax verifier.
     */
    static final class Verifier {
        private final List<Token> tokens;
        private int currentTokenIndex;
        private Token currentToken;

        public Verifier(List<Token> tokens) {
            this.tokens = tokens;
            this.currentTokenIndex = 0;
            this.currentToken = tokens.get(currentTokenIndex);
        }

        private void advance() {
            currentTokenIndex++;
            if (currentTokenIndex < tokens.size()) {
                currentToken = tokens.get(currentTokenIndex);
            } else {
                Token lastToken = tokens.get(currentTokenIndex - 1);
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

        private void verifyCondition() {
            verifyOr();
        }

        private void verifyOr() {
            verifyAnd();
            while (currentToken.getType() == TokenType.LOGIC_OR) {
                advance();
                verifyAnd();
            }
        }

        private void verifyAnd() {
            verifyCompare();
            while (currentToken.getType() == TokenType.LOGIC_AND) {
                advance();
                verifyCompare();
            }
        }

        private void verifyCompare() {
            if (currentToken.getType() == TokenType.LPAREN) {
                advance();
                verifyCondition();
                eat(TokenType.RPAREN);
                return;
            }
            if (currentToken.getType() == TokenType.LOGIC_NOT) {
                advance();
                verifyCompare();
                return;
            }
            verifyValueWithPipes();

            eat(TokenType.OPERATOR);

            verifyValueWithPipes();
        }

        private void verifyPipes() {
            while (nonEndToken(currentToken, TokenType.NEWLINE)) {
                if (currentToken.getType() == TokenType.PIPE_SYMBOL) {
                    advance();
                    eat(TokenType.IDENTIFIER);
                    verifyPipeParams();
                } else {
                    break;
                }
            }
        }

        private void verifyPipeParams() {
            if (currentToken.getType() == TokenType.LPAREN) {
                advance();
                while (nonEndToken(currentToken, TokenType.RPAREN, TokenType.NEWLINE)) {
                    verifyValue();
                    if (currentToken.getType() == TokenType.COMMA) {
                        advance();
                        if (currentToken.getType() == TokenType.RPAREN) {
                            throw new ScriptSyntaxException("Illegal token: " + TokenType.COMMA + " before " + currentToken);
                        }
                    } else {
                        break;
                    }
                }
                eat(TokenType.RPAREN);
            }
        }

        private void verifyVariableKeys() {
            eat(TokenType.IDENTIFIER);
            while (nonEndToken(currentToken, TokenType.NEWLINE)) {
                if (currentToken.getType() == TokenType.DOT) {
                    advance();
                    if (currentToken.getType() == TokenType.IDENTIFIER || currentToken.getType() == TokenType.NUMBER) {
                        advance();
                    } else {
                        throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + " / " + TokenType.NUMBER);
                    }
                } else if (currentToken.getType() == TokenType.LBRACKET) {
                    advance();
                    if (!StringUtils.isNonNegativeInteger(currentToken.getValue())) {
                        throw new ScriptSyntaxException("Index must be a non-negative integer: " + currentToken.getValue());
                    }
                    advance();
                    eat(TokenType.RBRACKET);
                } else {
                    break;
                }
            }
        }

        private void verifyNumber() {
            eat(TokenType.NUMBER);
            if (currentToken.getType() == TokenType.DOT) {
                advance();
                eat(TokenType.NUMBER);
            }
        }

        private void verifyValueWithPipes() {
            verifyValue();
            verifyPipes();
        }

        private void verifyValue() {
            switch (currentToken.getType()) {
                case IDENTIFIER:
                case STRING:
                case UNKNOWN:
                    advance();
                    break;
                case NUMBER:
                    verifyNumber();
                    break;
                case ADD_SYMBOL:
                case SUB_SYMBOL:
                    String sign = currentToken.getValue();
                    advance();
                    switch (currentToken.getType()) {
                        case NUMBER:
                            verifyNumber();
                            break;
                        case IDENTIFIER:
                        case STRING:
                        case UNKNOWN:
                            advance();
                            break;
                        default:
                            throw new ScriptSyntaxException("Symbol '" + sign + "' cannot at the start of " + currentToken);
                    }
                    break;
                case COLON:
                    advance();
                    verifyVariableKeys();
                    break;
                default:
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + " / " + TokenType.STRING + " / " + TokenType.NUMBER + " / :<variable> e.g. user, user.name, books[0]");
            }
        }

        private void verifyCaseValues() {
            verifyValueWithPipes();
            while (nonEndToken(currentToken, TokenType.NEWLINE)) {
                eat(TokenType.COMMA);
                verifyValueWithPipes();
            }
        }

        private void verifyIfStatement() {
            eat(TokenType.IF);
            verifyCondition();
            eat(TokenType.NEWLINE);
            // verify if block，until ELSE or END_IF
            doVerify(TokenType.ELSE, TokenType.END_IF);
            if (currentToken.getType() == TokenType.ELSE) {
                eat(TokenType.ELSE);
                eat(TokenType.NEWLINE);
                // verify else block，until END_IF
                doVerify(TokenType.END_IF);
            }
            eat(TokenType.END_IF);
            eat(TokenType.NEWLINE);
        }

        private void verifyGuardStatement() {
            eat(TokenType.GUARD);
            verifyCondition();
            eat(TokenType.NEWLINE);
            doVerify(TokenType.END_GUARD);
            eat(TokenType.END_GUARD);
            if (currentToken.getType() != TokenType.STRING && currentToken.getType() != TokenType.NEWLINE) {
                throw new ScriptSyntaxException("Illegal token: " + currentToken + ", excepted: " + TokenType.STRING + " / " + TokenType.NEWLINE);
            }
            if (currentToken.getType() == TokenType.STRING) {
                advance();
            }
            eat(TokenType.NEWLINE);
        }

        private void verifyCheckStatement() {
            eat(TokenType.CHECK);
            verifyCondition();
            eat(TokenType.CHECK_THROW);
            eat(TokenType.STRING);
            eat(TokenType.NEWLINE);
        }

        private void verifyVarStatement() {
            eat(TokenType.DEFINE_VAR);
            eat(TokenType.IDENTIFIER);
            if (currentToken.getType() == TokenType.OPERATOR && currentToken.getValue().equals("=")) {
                advance();
                verifyValueWithPipes();
                eat(TokenType.NEWLINE);
            } else {
                throw new ScriptSyntaxException("Unexcepted token: " + currentToken + ", excepted: '=' operator");
            }
        }

        private void verifySwitchStatement() {
            eat(TokenType.SWITCH);
            verifyValueWithPipes();
            eat(TokenType.NEWLINE);

            while (nonEndToken(currentToken, TokenType.END)) {
                if (currentToken.getType() == TokenType.CASE) {
                    advance();
                    verifyCaseValues();
                    eat(TokenType.NEWLINE);
                    doVerify(TokenType.BREAK);
                    eat(TokenType.BREAK);
                } else {
                    verifyDefaultBranch();
                }
            }
            eat(TokenType.END);
            eat(TokenType.NEWLINE);
        }

        private void verifyChooseStatement() {
            eat(TokenType.CHOOSE);
            eat(TokenType.NEWLINE);

            while (nonEndToken(currentToken, TokenType.END)) {
                if (currentToken.getType() == TokenType.WHEN) {
                    advance();
                    verifyCondition();
                    eat(TokenType.NEWLINE);
                    doVerify(TokenType.BREAK);
                    eat(TokenType.BREAK);
                } else {
                    verifyDefaultBranch();
                }
            }
            eat(TokenType.END);
            eat(TokenType.NEWLINE);
        }

        private void verifyDefaultBranch() {
            if (currentToken.getType() == TokenType.DEFAULT) {
                advance();
                eat(TokenType.NEWLINE);
                doVerify(TokenType.BREAK);
                eat(TokenType.BREAK);
            } else {
                eat(TokenType.NEWLINE);
            }
        }

        private void verifyForStatement() {
            eat(TokenType.FOR);
            String varName = currentToken.getValue();
            eat(TokenType.IDENTIFIER);
            if (currentToken.getType() == TokenType.COMMA) {
                advance();
                String idxName = currentToken.getValue();
                eat(TokenType.IDENTIFIER);
                if (varName.equals(idxName)) {
                    throw new ScriptSyntaxException("#for statement item and index must not have the same name: '" + varName + "', near " + currentToken);
                }
            }
            eat(TokenType.FOR_OF);
            verifyValueWithPipes();
            if (currentToken.getType() == TokenType.FOR_DELIMITER) {
                advance();
                eat(TokenType.STRING);
            }
            if (currentToken.getType() == TokenType.FOR_OPEN) {
                advance();
                eat(TokenType.STRING);
            }
            if (currentToken.getType() == TokenType.FOR_CLOSE) {
                advance();
                eat(TokenType.STRING);
            }
            eat(TokenType.NEWLINE);
            doVerify(TokenType.END_FOR);
            eat(TokenType.END_FOR);
            eat(TokenType.NEWLINE);
        }

        public void doVerify(TokenType... endTokens) {
            while (nonEndToken(currentToken, endTokens)) {
                switch (currentToken.getType()) {
                    case IF:
                        verifyIfStatement();
                        break;
                    case SWITCH:
                        verifySwitchStatement();
                        break;
                    case CHOOSE:
                        verifyChooseStatement();
                        break;
                    case FOR:
                        verifyForStatement();
                        break;
                    case GUARD:
                        verifyGuardStatement();
                        break;
                    case CHECK:
                        verifyCheckStatement();
                        break;
                    case DEFINE_VAR:
                        verifyVarStatement();
                        break;
                    case END_IF:
                    case ELSE:
                    case END:
                    case END_FOR:
                    case WHEN:
                    case CASE:
                    case DEFAULT:
                    case BREAK:
                    case END_GUARD:
                        throw new ScriptSyntaxException("Unexpected " + currentToken + " statement without preceding matching statement");
                    default:
                        advance();
                        break;
                }
            }
        }
    }
}
