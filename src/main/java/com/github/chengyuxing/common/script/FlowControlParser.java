package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.script.expression.IExpression;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.script.expression.impl.FastExpression;
import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.*;
import java.util.regex.Matcher;

import static com.github.chengyuxing.common.utils.ObjectUtil.coalesce;
import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;
import static com.github.chengyuxing.common.utils.StringUtil.isEmpty;

/**
 * <h2>Flow-Control parser</h2>
 * <p>if statement:</p>
 * <blockquote>
 * <pre>
 * #if {@linkplain FastExpression expression1}
 *      #if {@linkplain FastExpression expression2}
 *      ...
 *      #fi
 *      #if {@linkplain FastExpression expression3}
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
 * #for item[,idx] of :list [| {@linkplain IPipe pipe1} | pipeN | ... ] [delimiter ','] [open ''] [close '']
 *     ...
 * #done
 * </pre>
 * </blockquote>
 *
 * @see FastExpression
 */
public class FlowControlParser extends AbstractParser {
    private int forIndex = 0;

    @Override
    public String parse(String input, Map<String, Object> context) {
        forIndex = 0;
        forContextVars = new HashMap<>();
        FlowControlLexer lexer = new FlowControlLexer(input) {
            @Override
            protected String trimExpression(String line) {
                return FlowControlParser.this.trimExpression(line);
            }
        };
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, context);
        return parser.doParse();
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
            while ((currentToken.getType() != TokenType.END_FOR || forDepth != 0) && currentToken.getType() != TokenType.EOF) {
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

            Object variableValue = ObjectUtil.getDeepValue(context, variableName);

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
            String forCondition = parseCondition();
            eat(TokenType.NEWLINE);
            String forContent = parseForBlock();
            eat(TokenType.END_FOR);
            Matcher m = FOR_PATTERN.matcher(forCondition);
            if (m.find()) {
                String itemName = m.group("item");
                String idxName = m.group("index");
                String listName = m.group("list");
                String pipes = m.group("pipes");
                String delimiter = Comparators.getString(coalesce(m.group("delimiter"), ", ")) + NEW_LINE;
                String open = Comparators.getString(coalesce(m.group("open"), ""));
                String close = Comparators.getString(coalesce(m.group("close"), ""));
                //noinspection DataFlowIssue
                if (!open.isEmpty()) {
                    open = open + NEW_LINE;
                }
                //noinspection DataFlowIssue
                if (!close.isEmpty()) {
                    close = NEW_LINE + close;
                }

                Object listObject = ObjectUtil.getDeepValue(context, listName);
                if (!isEmpty(pipes)) {
                    listObject = expression("empty").pipedValue(listObject, pipes);
                }
                Object[] iterator = ObjectUtil.toArray(listObject);

                StringJoiner result = new StringJoiner(delimiter);
                Map<String, Object> localForVars = new HashMap<>();
                for (int i = 0, j = iterator.length; i < j; i++) {
                    Object item = iterator[i];

                    Map<String, Object> childContext = new HashMap<>(context);

                    if (itemName != null) {
                        localForVars.put(forVarKey(itemName, forIndex, i), item);
                        childContext.put(itemName, item);
                    }
                    if (idxName != null) {
                        localForVars.put(forVarKey(idxName, forIndex, i), i);
                        childContext.put(idxName, i);
                    }

                    String formattedForContent = forLoopBodyFormatter(forIndex, i, itemName, idxName, forContent, childContext);

                    FlowControlLexer forLoopContentLexer = new FlowControlLexer(formattedForContent);
                    List<Token> forLoopTokens = forLoopContentLexer.tokenize();
                    Parser parser = new Parser(forLoopTokens, childContext);
                    String forContentResult = parser.doParse();

                    if (!forContentResult.trim().isEmpty()) {
                        result.add(forContentResult);
                    }
                }
                forIndex++;
                String resultFor = result.toString().trim();
                if (resultFor.isEmpty()) {
                    return resultFor;
                }
                forContextVars.putAll(localForVars);
                return open + resultFor + close;
            } else {
                throw new ScriptSyntaxException("#for syntax error of expression '" + forCondition + "'");
            }
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
            return result.toString().trim().replaceAll("\\s*\r?\n", NEW_LINE);
        }
    }
}
