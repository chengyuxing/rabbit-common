package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.script.expression.IExpression;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.script.expression.impl.FastExpression;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * <h2>Flow-Control parser</h2>
 * <p>if statement:</p>
 * <blockquote>
 * <pre>
 * #if {@linkplain FastExpression expression1}
 *      #if {@linkplain FastExpression expression2}
 *      ...
 *      #fi
 *      #if {@linkplain FastExpression expression2}
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
 *      #when {@linkplain FastExpression expression1}
 *      ...
 *      #break
 *      #when {@linkplain FastExpression expression2}
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
 *      #case var1
 *      ...
 *      #break
 *      #case var2
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
 * #for item of :list [delimiter ', '] [open ''] [close '']
 *     ...
 * #done
 * </pre>
 * </blockquote>
 *
 * @see FastExpression
 */
public class FlowControlParser {
    private int forIndex = 0;
    private Map<String, Object> forContextVars = new HashMap<>();

    /**
     * Parse content with scripts.
     *
     * @return parsed content
     * @see IExpression
     */
    public String parse(String input, Map<String, Object> context) {
        forIndex = 0;
        forContextVars = new HashMap<>();
        List<Token> tokens = lexer(input).tokenize();
        Parser parser = new Parser(tokens, context);
        return parser.doParse();
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
        return forContextVars;
    }

    /**
     * Lexer.
     *
     * @param input input
     * @return new Lexer instance
     */
    protected FlowControlLexer lexer(String input) {
        return new FlowControlLexer(input);
    }

    /**
     * Configure expression parser implementation.
     *
     * @param expression expression
     * @return expression parser implementation
     */
    protected IExpression expression(String expression) {
        return FastExpression.of(expression);
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
     * @param body     content in for loop
     * @param context  each for loop context args (index and value) which created by for expression
     * @return formatted content
     * @see #getForContextVars()
     */
    protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String body, Map<String, Object> context) {
        return StringUtil.FMT.format(body, context);
    }

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
                throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + type);
            }
        }

        private boolean evaluateCondition(String condition) {
            return expression(condition).calc(context);
        }

        private String parseCondition() {
            StringJoiner condition = new StringJoiner(" ");
            while (currentToken.getType() != TokenType.NEWLINE && currentToken.getType() != TokenType.EOF) {
                condition.add(currentToken.getValue());
                advance();
            }
            return condition.toString().trim();
        }

        private String parseForBlock() {
            StringJoiner condition = new StringJoiner(" ");
            int forDepth = 0;
            while (currentToken.getType() != TokenType.END_FOR || forDepth != 0) {
                if (currentToken.getType() == TokenType.FOR) {
                    forDepth++;
                } else if (currentToken.getType() == TokenType.END_FOR) {
                    forDepth--;
                }
                condition.add(currentToken.getValue());
                advance();
            }
            return condition.toString().trim();
        }

        private String doParseStatement() {
            String result;
            if (currentToken.getType() == TokenType.IF) {
                result = parseIfStatement();
            } else if (currentToken.getType() == TokenType.SWITCH) {
                result = parseSwitchStatement();
            } else if (currentToken.getType() == TokenType.CHOOSE) {
                result = parseChooseStatement();
            } else if (currentToken.getType() == TokenType.FOR) {
                result = parseForStatement();
            } else {
                result = currentToken.getValue();
                advance();
            }
            return result;
        }

        private String parseContent() {
            StringJoiner content = new StringJoiner(" ");
            while (currentToken.getType() != TokenType.ELSE &&
                    currentToken.getType() != TokenType.ENDIF &&
                    currentToken.getType() != TokenType.END_FOR &&
                    currentToken.getType() != TokenType.BREAK &&
                    currentToken.getType() != TokenType.END &&
                    currentToken.getType() != TokenType.EOF) {
                content.add(doParseStatement());
            }
            return content.toString();
        }

        private String parseIfStatement() {
            eat(TokenType.IF);
            String condition = parseCondition();
            eat(TokenType.NEWLINE);
            String thenContent = parseContent();
            String elseContent = "";
            if (currentToken.getType() == TokenType.ELSE) {
                advance();
                elseContent = parseContent();
            }
            eat(TokenType.ENDIF);
            boolean conditionSatisfied = evaluateCondition(condition);
            return conditionSatisfied ? thenContent : elseContent;
        }

        private String parseSwitchStatement() {
            eat(TokenType.SWITCH);
            String variable = parseCondition();
            eat(TokenType.NEWLINE);

            Map<String, String> caseContentMap = new HashMap<>();
            String defaultContent = "";

            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.CASE) {
                    advance();
                    String caseValue = parseCondition();
                    eat(TokenType.NEWLINE);
                    String caseContent = parseContent();
                    caseContentMap.put(caseValue, caseContent);
                    if (currentToken.getType() == TokenType.BREAK) {
                        advance();
                    }
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    defaultContent = parseContent();
                    eat(TokenType.NEWLINE);
                    if (currentToken.getType() == TokenType.BREAK) {
                        advance();
                    }
                } else {
                    if (currentToken.getType() == TokenType.NEWLINE) {
                        advance();
                    } else {
                        throw new ScriptSyntaxException("Unexpected token in switch statement: " + currentToken);
                    }
                }
            }
            eat(TokenType.END);

            String variableName = variable.substring(1);
            String pipes = "";

            int pipeIdx = variableName.indexOf('|');
            if (pipeIdx != -1) {
                pipes = variableName.substring(pipeIdx);
                variableName = variableName.substring(0, pipeIdx).trim();
            }

            Object variableValue = context.get(variableName);

            if (!pipes.trim().isEmpty()) {
                variableValue = expression("empty").pipedValue(variableValue, pipes);
            }

            for (Map.Entry<String, String> entry : caseContentMap.entrySet()) {
                if (Comparators.compare(variableValue, "=", Comparators.valueOf(entry.getKey()))) {
                    return entry.getValue();
                }
            }
            return defaultContent;
        }

        private String parseChooseStatement() {
            eat(TokenType.CHOOSE);
            eat(TokenType.NEWLINE);

            Map<String, String> whenContentMap = new HashMap<>();
            String defaultContent = "";

            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.WHEN) {
                    advance();
                    String condition = parseCondition();
                    eat(TokenType.NEWLINE);
                    String whenContent = parseContent();
                    whenContentMap.put(condition, whenContent);
                    if (currentToken.getType() == TokenType.BREAK) {
                        advance();
                    }
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    defaultContent = parseContent();
                    if (currentToken.getType() == TokenType.BREAK) {
                        advance();
                    }
                } else {
                    if (currentToken.getType() == TokenType.NEWLINE) {
                        advance();
                    } else {
                        throw new ScriptSyntaxException("Unexpected token in choose statement: " + currentToken);
                    }
                }
            }
            eat(TokenType.END);

            for (Map.Entry<String, String> entry : whenContentMap.entrySet()) {
                if (evaluateCondition(entry.getKey())) {
                    return entry.getValue();
                }
            }
            return defaultContent;
        }

        private String parseForStatement() {
            eat(TokenType.FOR);
            String itemVariable = currentToken.getValue();
            eat(TokenType.IDENTIFIER);

            eat(TokenType.FOR_OF);
            String listVariable = currentToken.getValue();
            eat(TokenType.VARIABLE_NAME);

            String delimiter = ", ";
            if (currentToken.getType() == TokenType.FOR_DELIMITER) {
                advance();
                delimiter = Comparators.getString(currentToken.getValue());
                advance();
            }
            String open = "";
            if (currentToken.getType() == TokenType.FOR_OPEN) {
                advance();
                open = Comparators.getString(currentToken.getValue());
                if (!open.isEmpty()) {
                    open = open + '\n';
                }
                advance();
            }
            String close = "";
            if (currentToken.getType() == TokenType.FOR_CLOSE) {
                advance();
                close = Comparators.getString(currentToken.getValue());
                if (!close.isEmpty()) {
                    close = '\n' + close;
                }
                advance();
            }

            eat(TokenType.NEWLINE);
            String forContent = parseForBlock();
            eat(TokenType.END_FOR);

            String listName = listVariable.substring(1);
            String pipes = "";
            int pipeIdx = listName.indexOf('|');
            if (pipeIdx != -1) {
                pipes = listName.substring(pipeIdx);
                listName = listName.substring(0, pipeIdx);
            }

            Object listObject = ObjectUtil.getDeepValue(context, listName);

            if (!pipes.trim().isEmpty()) {
                listObject = expression("empty").pipedValue(listObject, pipes);
            }

            Object[] iterator = ObjectUtil.toArray(listObject);

            StringJoiner result = new StringJoiner(delimiter + '\n', open, close);
            for (int i = 0, j = iterator.length; i < j; i++) {
                Object item = iterator[i];

                Map<String, Object> childContext = new HashMap<>(context);
                childContext.put(itemVariable, item);

                String formattedForContent = forLoopBodyFormatter(forIndex, i, itemVariable, forContent, childContext);

                Parser parser = new Parser(lexer(formattedForContent).tokenize(), childContext);
                String forContentResult = parser.doParse();

                if (!forContentResult.trim().isEmpty()) {
                    forContextVars.put(forVarKey(itemVariable, forIndex, i), item);
                    result.add(forContentResult);
                }
            }
            forIndex++;
            return result.toString();
        }

        /**
         * Parse tokens.
         *
         * @return parsed tokens
         * @see IExpression
         */
        public String doParse() {
            if (tokens.isEmpty()) {
                return "";
            }
            StringBuilder result = new StringBuilder();
            while (currentToken.getType() != TokenType.EOF) {
                result.append(doParseStatement());
            }
            return result.toString().trim();
        }
    }
}
