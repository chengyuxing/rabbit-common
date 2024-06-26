package com.github.chengyuxing.common.script.parser;

import com.github.chengyuxing.common.script.exception.PipeNotFoundException;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.*;

import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;

/**
 * <h2>Based lexer tokens Flow-Control parser.</h2>
 * <p>if statement:</p>
 * <blockquote>
 * <pre>
 * #if {@linkplain Parser#evaluateCondition() expression1}
 *      #if {@linkplain Parser#evaluateCondition() expression2}
 *      ...
 *      #fi
 *      #if {@linkplain Parser#evaluateCondition() expression3}
 *      ...
 *      #else
 *      ...
 *      #fi
 * #fi
 * </pre>
 * </blockquote>
 * <p>choose statement:</p>
 * <blockquote>
 * <pre>
 * #choose
 *      #when {@linkplain Parser#evaluateCondition() expression1}
 *      ...
 *      #break
 *      #when {@linkplain Parser#evaluateCondition() expression2}
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
 *
 * @see Comparators
 */
public class FlowControlParser {
    private static final Map<String, IPipe<?>> GLOBAL_PIPES = new HashMap<>();
    private Map<String, IPipe<?>> customPipes = new HashMap<>();

    private final List<Token> tokens;

    private int forIndex = 0;
    private Map<String, Object> forContextVars = new HashMap<>();

    static {
        GLOBAL_PIPES.put("length", new IPipe.Length());
        GLOBAL_PIPES.put("upper", new IPipe.Upper());
        GLOBAL_PIPES.put("lower", new IPipe.Lower());
        GLOBAL_PIPES.put("pairs", new IPipe.Map2Pairs());
        GLOBAL_PIPES.put("kv", new IPipe.Kv());
    }

    /**
     * Construct a new FlowControlParser with input content.
     *
     * @param input content with flow-control scripts
     */
    public FlowControlParser(String input) {
        FlowControlLexer lexer = new FlowControlLexer(input) {
            @Override
            protected String trimExpression(String line) {
                return FlowControlParser.this.trimExpression(line);
            }
        };
        this.tokens = lexer.tokenize();
    }

    /**
     * Parse content with scripts.
     *
     * @param context context params
     * @return parsed content
     */
    public String parse(Map<String, Object> context) {
        if (tokens.isEmpty()) {
            return "";
        }
        forIndex = 0;
        forContextVars = new HashMap<>();
        Parser parser = new Parser(tokens, context);
        return parser.doParse();
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
        if (this.customPipes.equals(pipes)) {
            return;
        }
        this.customPipes = new HashMap<>(pipes);
    }

    /**
     * Get custom pipes.
     *
     * @return custom pipes
     */
    protected Map<String, IPipe<?>> getPipes() {
        return customPipes;
    }

    /**
     * Trim each line for search prefix {@code #} to detect expression.
     *
     * @param line current line
     * @return expression or normal line
     */
    protected String trimExpression(String line) {
        String tl = line.trim();
        if (tl.startsWith("#")) {
            return tl;
        }
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
     * @param forIndex each for loop auto index
     * @param varIndex for var auto index
     * @param varName  for context var name,  e.g. {@code <user>}
     * @param idxName  for context index name,  e.g. {@code <idx>}
     * @param body     content in for loop
     * @param context  each for loop context args (index and value) which created by for expression
     * @return formatted content
     * @see #getForContextVars()
     */
    protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, String body, Map<String, Object> context) {
        return StringUtil.FMT.format(body, context);
    }

    /**
     * Build {@code #for} var key.
     *
     * @param name   for context var name
     * @param forIdx for auto index
     * @param varIdx var auto index
     * @return unique for var key
     */
    protected String forVarKey(String name, int forIdx, int varIdx) {
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
     * @see #forVarKey(String, int, int)
     */
    public Map<String, Object> getForContextVars() {
        return Collections.unmodifiableMap(forContextVars);
    }

    /**
     * Parser implementation.
     */
    final class Parser {
        private final List<Token> tokens;
        private int currentTokenIndex;
        private Token currentToken;
        private final Map<String, Object> context;

        public Parser(List<Token> tokens, Map<String, Object> context) {
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
                currentToken = new Token(TokenType.EOF, "");
            }
        }

        private void eat(TokenType type) {
            if (currentToken.getType() == type) {
                advance();
            } else {
                throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + type + ", At: " + currentTokenIndex);
            }
        }

        /**
         * <p>Boolean condition expression.</p>
         * <p>Support logic operator: {@code &amp;&amp;, ||, !}, e.g.</p>
         * <blockquote><pre>!(:id &gt;= 0 || :name | {@link IPipe.Length length} &lt;= 3) &amp;&amp; :age &gt; 21
         * </pre></blockquote>
         * Built-in {@link IPipe pipes}：
         * <ul>
         *     <li>{@link IPipe.Length length}</li>
         *     <li>{@link IPipe.Upper upper}</li>
         *     <li>{@link IPipe.Lower lower}</li>
         *     <li>{@link IPipe.Map2Pairs pairs}</li>
         *     <li>{@link IPipe.Kv kv}</li>
         * </ul>
         *
         * @return is matched or not
         * @see Comparators
         */
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

            Token left = parseBoolExpressionItem();
            advance();
            List<String> leftPipes = new ArrayList<>();
            if (currentToken.getType() == TokenType.PIPE_SYMBOL) {
                leftPipes = parsePipes();
            }

            String operator = currentToken.getValue();
            eat(TokenType.OPERATOR);

            Token right = parseBoolExpressionItem();
            advance();
            List<String> rightPipes = new ArrayList<>();
            if (currentToken.getType() == TokenType.PIPE_SYMBOL) {
                rightPipes = parsePipes();
            }

            Object a = parseValue(left, leftPipes);
            Object b = parseValue(right, rightPipes);

            return Comparators.compare(a, operator, b);
        }

        private Token parseBoolExpressionItem() {
            switch (currentToken.getType()) {
                case IDENTIFIER:
                case STRING:
                case NUMBER:
                case VARIABLE_NAME:
                    return currentToken;
                default:
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + "/" + TokenType.STRING + "/" + TokenType.NUMBER + "/" + TokenType.VARIABLE_NAME + ", At: " + currentTokenIndex);
            }
        }

        private List<String> parsePipes() {
            List<String> pipes = new ArrayList<>();
            while (currentToken.getType() != TokenType.FOR_DELIMITER &&
                    currentToken.getType() != TokenType.FOR_OPEN &&
                    currentToken.getType() != TokenType.FOR_CLOSE &&
                    currentToken.getType() != TokenType.OPERATOR &&
                    currentToken.getType() != TokenType.LOGIC_OR &&
                    currentToken.getType() != TokenType.LOGIC_AND &&
                    currentToken.getType() != TokenType.RPAREN &&
                    currentToken.getType() != TokenType.NEWLINE &&
                    currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.PIPE_SYMBOL) {
                    advance();
                    pipes.add(currentToken.getValue());
                    eat(TokenType.IDENTIFIER);
                } else {
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.PIPE_SYMBOL + ", At: " + currentTokenIndex);
                }
            }
            return pipes;
        }

        private Object parsePipedValue(Object value, List<String> pipes) {
            Object res = value;
            for (String pipe : pipes) {
                if (getPipes().containsKey(pipe)) {
                    res = getPipes().get(pipe).transform(res);
                } else if (GLOBAL_PIPES.containsKey(pipe)) {
                    res = GLOBAL_PIPES.get(pipe).transform(res);
                } else {
                    throw new PipeNotFoundException("Cannot find pipe '" + pipe + "'");
                }
            }
            return res;
        }

        private Object parseValue(Token token, List<String> pipes) {
            if (token.getType() == TokenType.VARIABLE_NAME) {
                Object value = ObjectUtil.getDeepValue(context, token.getValue().substring(1));
                if (!pipes.isEmpty()) {
                    value = parsePipedValue(value, pipes);
                }
                return value;
            }
            // string literal value
            Object value = token.getValue();
            if (!pipes.isEmpty()) {
                value = parsePipedValue(value, pipes);
            }
            return Comparators.valueOf(value);
        }

        private String parseCaseLiteralValue() {
            switch (currentToken.getType()) {
                case IDENTIFIER:
                case STRING:
                case NUMBER:
                    return currentToken.getValue();
                default:
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + "/" + TokenType.STRING + "/" + TokenType.NUMBER + ", At: " + currentTokenIndex);
            }
        }

        private List<String> parseCaseLiteralValues() {
            List<String> values = new ArrayList<>();
            values.add(parseCaseLiteralValue());
            advance();
            while (currentToken.getType() != TokenType.NEWLINE &&
                    currentToken.getType() != TokenType.EOF) {
                eat(TokenType.COMMA);
                values.add(parseCaseLiteralValue());
                advance();
            }
            return values;
        }

        private List<Token> parseForBlock() {
            List<Token> tokens = new ArrayList<>();
            int forDepth = 0;
            while ((currentToken.getType() != TokenType.END_FOR || forDepth != 0) && currentToken.getType() != TokenType.EOF) {
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

        private List<Token> parseBranchBlock() {
            List<Token> caseWhenDefaultBlock = new ArrayList<>();
            while (currentToken.getType() != TokenType.BREAK && currentToken.getType() != TokenType.EOF) {
                caseWhenDefaultBlock.add(currentToken);
                advance();
            }
            return caseWhenDefaultBlock;
        }

        private void goToEnd() {
            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                advance();
            }
        }

        private String parseIfStatement() {
            eat(TokenType.IF);
            boolean matched = evaluateCondition();
            eat(TokenType.NEWLINE);
            List<Token> ifBlockContent = new ArrayList<>();
            int ifDepth = 1;
            int elseIndex = -1;
            int index = 0;
            while ((currentToken.getType() != TokenType.ENDIF || ifDepth != 1) && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.IF) {
                    ifDepth++;
                } else if (currentToken.getType() == TokenType.ENDIF) {
                    ifDepth--;
                } else if (currentToken.getType() == TokenType.ELSE && ifDepth == 1) {
                    elseIndex = index;
                }
                ifBlockContent.add(currentToken);
                index++;
                advance();
            }
            eat(TokenType.ENDIF);
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
            Parser parser = new Parser(matchedContent, context);
            return parser.doParse();
        }

        private String parseSwitchStatement() {
            eat(TokenType.SWITCH);
            Token variable = currentToken;
            eat(TokenType.VARIABLE_NAME);
            List<String> pipes = parsePipes();
            eat(TokenType.NEWLINE);

            Object variableValue = parseValue(variable, pipes);

            List<Token> matchedBranch = new ArrayList<>();
            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.CASE) {
                    advance();
                    List<String> caseValues = parseCaseLiteralValues();
                    eat(TokenType.NEWLINE);
                    List<Token> caseContent = parseBranchBlock();
                    eat(TokenType.BREAK);

                    for (String caseValue : caseValues) {
                        if (Comparators.compare(variableValue, "=", Comparators.valueOf(caseValue))) {
                            matchedBranch = caseContent;
                            goToEnd();
                            break;
                        }
                    }
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    matchedBranch = parseBranchBlock();
                    eat(TokenType.BREAK);
                } else {
                    eat(TokenType.NEWLINE);
                }
            }
            eat(TokenType.END);
            eat(TokenType.NEWLINE);

            if (matchedBranch.isEmpty()) {
                return "";
            }
            Parser parser = new Parser(matchedBranch, context);
            return parser.doParse();
        }

        private String parseChooseStatement() {
            eat(TokenType.CHOOSE);
            eat(TokenType.NEWLINE);

            List<Token> matchedBranch = new ArrayList<>();

            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.WHEN) {
                    advance();
                    boolean matched = evaluateCondition();
                    eat(TokenType.NEWLINE);
                    List<Token> whenContent = parseBranchBlock();
                    eat(TokenType.BREAK);

                    if (matched) {
                        matchedBranch = whenContent;
                        goToEnd();
                    }
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    matchedBranch = parseBranchBlock();
                    eat(TokenType.BREAK);
                } else {
                    eat(TokenType.NEWLINE);
                }
            }
            eat(TokenType.END);
            eat(TokenType.NEWLINE);

            if (matchedBranch.isEmpty()) {
                return "";
            }
            Parser parser = new Parser(matchedBranch, context);
            return parser.doParse();
        }

        private String parseForStatement() {
            eat(TokenType.FOR);
            String varName = currentToken.getValue();
            eat(TokenType.IDENTIFIER);
            String idxName = "";
            if (currentToken.getType() == TokenType.COMMA) {
                advance();
                idxName = currentToken.getValue();
                eat(TokenType.IDENTIFIER);
            }
            eat(TokenType.FOR_OF);
            Token listName = currentToken;
            eat(TokenType.VARIABLE_NAME);

            List<String> pipes = parsePipes();

            String delimiter = ", ";
            String open = "";
            String close = "";
            if (currentToken.getType() == TokenType.FOR_DELIMITER) {
                advance();
                delimiter = Comparators.getString(currentToken.getValue());
                eat(TokenType.STRING);
            }
            if (currentToken.getType() == TokenType.FOR_OPEN) {
                advance();
                open = Comparators.getString(currentToken.getValue());
                eat(TokenType.STRING);
            }
            if (currentToken.getType() == TokenType.FOR_CLOSE) {
                advance();
                close = Comparators.getString(currentToken.getValue());
                eat(TokenType.STRING);
            }

            eat(TokenType.NEWLINE);
            List<Token> forContent = parseForBlock();
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

            Object listObject = parseValue(listName, pipes);
            Object[] iterator = ObjectUtil.toArray(listObject);

            StringJoiner result = new StringJoiner(delimiter + '\n');
            Map<String, Object> localForVars = new HashMap<>();

            for (int i = 0, j = iterator.length; i < j; i++) {
                Object item = iterator[i];

                Map<String, Object> childContext = new HashMap<>(context);

                if (!varName.isEmpty()) {
                    localForVars.put(forVarKey(varName, forIndex, i), item);
                    childContext.put(varName, item);
                }
                if (!idxName.isEmpty()) {
                    localForVars.put(forVarKey(idxName, forIndex, i), i);
                    childContext.put(idxName, i);
                }

                // for loop body content tokens.
                List<Token> newForContent = new ArrayList<>(forContent.size());
                for (Token token : forContent) {
                    if (token.getType() == TokenType.PLAIN_TEXT) {
                        String old = token.getValue();
                        String newValue = forLoopBodyFormatter(forIndex, i, varName, idxName, old, childContext);
                        Token newToken = new Token(token.getType(), newValue);
                        newForContent.add(newToken);
                        continue;
                    }
                    newForContent.add(token);
                }

                Parser parser = new Parser(newForContent, childContext);
                String forContentResult = parser.doParse();

                if (!forContentResult.trim().isEmpty()) {
                    result.add(forContentResult);
                }
            }
            forIndex++;
            String resultFor = result.toString().trim();
            if (resultFor.isEmpty()) {
                return "";
            }
            forContextVars.putAll(localForVars);
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
            StringBuilder result = new StringBuilder();
            while (currentToken.getType() != TokenType.EOF) {
                switch (currentToken.getType()) {
                    case IF:
                        result.append(parseIfStatement());
                        break;
                    case SWITCH:
                        result.append(parseSwitchStatement());
                        break;
                    case CHOOSE:
                        result.append(parseChooseStatement());
                        break;
                    case FOR:
                        result.append(parseForStatement());
                        break;
                    default:
                        result.append(currentToken.getValue());
                        advance();
                        break;
                }
            }
            return result.toString().trim().replaceAll("\\s*\r?\n", NEW_LINE);
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
                currentToken = new Token(TokenType.EOF, "");
            }
        }

        private void eat(TokenType type) {
            if (currentToken.getType() == type) {
                advance();
            } else {
                throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + type + ", At: " + currentTokenIndex);
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
            verifyBoolExpressionItem();
            advance();
            if (currentToken.getType() == TokenType.PIPE_SYMBOL) {
                verifyPipes();
            }

            eat(TokenType.OPERATOR);

            verifyBoolExpressionItem();
            advance();
            if (currentToken.getType() == TokenType.PIPE_SYMBOL) {
                verifyPipes();
            }
        }

        private void verifyBoolExpressionItem() {
            switch (currentToken.getType()) {
                case IDENTIFIER:
                case STRING:
                case NUMBER:
                case VARIABLE_NAME:
                    break;
                default:
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + "/" + TokenType.STRING + "/" + TokenType.NUMBER + "/" + TokenType.VARIABLE_NAME + ", At: " + currentTokenIndex);
            }
        }

        private void verifyPipes() {
            while (currentToken.getType() != TokenType.FOR_DELIMITER &&
                    currentToken.getType() != TokenType.FOR_OPEN &&
                    currentToken.getType() != TokenType.FOR_CLOSE &&
                    currentToken.getType() != TokenType.OPERATOR &&
                    currentToken.getType() != TokenType.LOGIC_OR &&
                    currentToken.getType() != TokenType.LOGIC_AND &&
                    currentToken.getType() != TokenType.RPAREN &&
                    currentToken.getType() != TokenType.NEWLINE &&
                    currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.PIPE_SYMBOL) {
                    advance();
                    eat(TokenType.IDENTIFIER);
                } else {
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.PIPE_SYMBOL + ", At: " + currentTokenIndex);
                }
            }
        }

        private void verifyCaseLiteralValue() {
            switch (currentToken.getType()) {
                case IDENTIFIER:
                case STRING:
                case NUMBER:
                    break;
                default:
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + "/" + TokenType.STRING + "/" + TokenType.NUMBER + ", At: " + currentTokenIndex);
            }
        }

        private void verifyCaseLiteralValues() {
            verifyCaseLiteralValue();
            advance();
            while (currentToken.getType() != TokenType.NEWLINE &&
                    currentToken.getType() != TokenType.EOF) {
                eat(TokenType.COMMA);
                verifyCaseLiteralValue();
                advance();
            }
        }

        private void verifyContent() {
            while (currentToken.getType() != TokenType.END &&
                    currentToken.getType() != TokenType.BREAK &&
                    currentToken.getType() != TokenType.ELSE &&
                    currentToken.getType() != TokenType.ENDIF &&
                    currentToken.getType() != TokenType.END_FOR &&
                    currentToken.getType() != TokenType.EOF) {
                doVerifyStatement();
            }
        }

        private void doVerifyStatement() {
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
                default:
                    advance();
                    break;
            }
        }

        private void verifyIfStatement() {
            eat(TokenType.IF);
            verifyCondition();
            eat(TokenType.NEWLINE);
            verifyContent();
            if (currentToken.getType() == TokenType.ELSE) {
                advance();
                verifyContent();
            }
            eat(TokenType.ENDIF);
            eat(TokenType.NEWLINE);
        }

        private void verifySwitchStatement() {
            eat(TokenType.SWITCH);
            eat(TokenType.VARIABLE_NAME);
            verifyPipes();
            eat(TokenType.NEWLINE);

            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.CASE) {
                    advance();
                    verifyCaseLiteralValues();
                    eat(TokenType.NEWLINE);
                    verifyContent();
                    eat(TokenType.BREAK);
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    verifyContent();
                    eat(TokenType.BREAK);
                } else {
                    eat(TokenType.NEWLINE);
                }
            }
            eat(TokenType.END);
            eat(TokenType.NEWLINE);
        }

        private void verifyChooseStatement() {
            eat(TokenType.CHOOSE);
            eat(TokenType.NEWLINE);

            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.WHEN) {
                    advance();
                    verifyCondition();
                    eat(TokenType.NEWLINE);
                    verifyContent();
                    eat(TokenType.BREAK);
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    verifyContent();
                    eat(TokenType.BREAK);
                } else {
                    eat(TokenType.NEWLINE);
                }
            }
            eat(TokenType.END);
            eat(TokenType.NEWLINE);
        }

        private void verifyForStatement() {
            eat(TokenType.FOR);
            eat(TokenType.IDENTIFIER);
            if (currentToken.getType() == TokenType.COMMA) {
                advance();
                eat(TokenType.IDENTIFIER);
            }
            eat(TokenType.FOR_OF);
            eat(TokenType.VARIABLE_NAME);
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
            verifyContent();
            eat(TokenType.END_FOR);
            eat(TokenType.NEWLINE);
        }

        public void doVerify() {
            while (currentToken.getType() != TokenType.EOF) {
                doVerifyStatement();
            }
        }
    }
}
