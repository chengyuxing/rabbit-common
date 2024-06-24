package com.github.chengyuxing.common.script.parser;

import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.script.expression.impl.FastExpression;
import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.script.expression.IExpression;
import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.*;

import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;
import static com.github.chengyuxing.common.utils.StringUtil.isEmpty;

/**
 * <h2>Based lexer tokens Flow-Control parser.</h2>
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
 * @see FastExpression
 */
public class FlowControlParser extends AbstractParser {
    private int forIndex = 0;

    @Override
    public String parse(String input, Map<String, Object> context) {
        if (isEmpty(input)) {
            return "";
        }
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
                throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + type + ", At: " + currentTokenIndex);
            }
        }

        private boolean evaluateCondition(String condition) {
            return expression(condition).calc(context);
        }

        private String parseCondition() {
            StringBuilder condition = new StringBuilder();
            while (currentToken.getType() != TokenType.NEWLINE && currentToken.getType() != TokenType.EOF) {
                condition.append(currentToken.getValue());
                advance();
            }
            return condition.toString();
        }

        private String parsePipeLine() {
            StringBuilder sb = new StringBuilder();
            while (currentToken.getType() != TokenType.FOR_DELIMITER &&
                    currentToken.getType() != TokenType.FOR_OPEN &&
                    currentToken.getType() != TokenType.FOR_CLOSE &&
                    currentToken.getType() != TokenType.NEWLINE &&
                    currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.LOGIC_OR) {
                    sb.append(currentToken.getValue());
                    advance();
                    sb.append(currentToken.getValue());
                    eat(TokenType.IDENTIFIER);
                } else {
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.LOGIC_OR + ", At: " + currentTokenIndex);
                }
            }
            return sb.toString();
        }

        private String parseCaseValue() {
            switch (currentToken.getType()) {
                case IDENTIFIER:
                case STRING:
                case NUMBER:
                    return currentToken.getValue();
                default:
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + "/" + TokenType.STRING + "/" + TokenType.NUMBER + ", At: " + currentTokenIndex);
            }
        }

        private List<String> parseCaseValues() {
            List<String> values = new ArrayList<>();
            values.add(parseCaseValue());
            advance();
            while (currentToken.getType() != TokenType.NEWLINE &&
                    currentToken.getType() != TokenType.EOF) {
                eat(TokenType.COMMA);
                values.add(parseCaseValue());
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

        private String parseIfStatement() {
            eat(TokenType.IF);
            String condition = parseCondition();
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

            List<Token> thenContent;
            List<Token> elseContent = new ArrayList<>();
            if (elseIndex == -1) {
                thenContent = ifBlockContent;
            } else {
                thenContent = ifBlockContent.subList(0, elseIndex);
                elseContent = ifBlockContent.subList(elseIndex + 1, ifBlockContent.size());
            }

            List<Token> matchedContent = evaluateCondition(condition) ? thenContent : elseContent;
            if (matchedContent.isEmpty()) {
                return "";
            }
            Parser parser = new Parser(matchedContent, context);
            return parser.doParse();
        }

        private String parseSwitchStatement() {
            eat(TokenType.SWITCH);
            String variable = currentToken.getValue();
            eat(TokenType.VARIABLE_NAME);
            String pipes = parsePipeLine();
            eat(TokenType.NEWLINE);

            Map<List<String>, List<Token>> caseContentMap = new LinkedHashMap<>();
            List<Token> matchedCaseContent = new ArrayList<>();

            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.CASE) {
                    advance();
                    List<String> caseValue = parseCaseValues();
                    eat(TokenType.NEWLINE);
                    List<Token> caseContent = parseBranchBlock();
                    caseContentMap.put(caseValue, caseContent);
                    if (currentToken.getType() == TokenType.BREAK) {
                        advance();
                    }
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    matchedCaseContent = parseBranchBlock();
                    if (currentToken.getType() == TokenType.BREAK) {
                        advance();
                    }
                } else {
                    if (currentToken.getType() == TokenType.NEWLINE) {
                        advance();
                    } else {
                        throw new ScriptSyntaxException("Unexpected token in switch statement: " + currentToken + ", At: " + currentTokenIndex);
                    }
                }
            }
            eat(TokenType.END);

            String variableName = variable.substring(1);

            Object variableValue = ObjectUtil.getDeepValue(context, variableName);

            if (!pipes.trim().isEmpty()) {
                variableValue = expression("empty").pipedValue(variableValue, pipes);
            }

            caseBranch:
            for (Map.Entry<List<String>, List<Token>> entry : caseContentMap.entrySet()) {
                List<String> caseValues = entry.getKey();
                for (String caseValue : caseValues) {
                    if (Comparators.compare(variableValue, "=", Comparators.valueOf(caseValue))) {
                        matchedCaseContent = entry.getValue();
                        break caseBranch;
                    }
                }
            }
            if (matchedCaseContent.isEmpty()) {
                return "";
            }
            Parser parser = new Parser(matchedCaseContent, context);
            return parser.doParse();
        }

        private String parseChooseStatement() {
            eat(TokenType.CHOOSE);
            eat(TokenType.NEWLINE);

            Map<String, List<Token>> whenContentMap = new LinkedHashMap<>();
            List<Token> matchedWhenContent = new ArrayList<>();

            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.WHEN) {
                    advance();
                    String condition = parseCondition();
                    eat(TokenType.NEWLINE);
                    List<Token> whenContent = parseBranchBlock();
                    whenContentMap.put(condition, whenContent);
                    if (currentToken.getType() == TokenType.BREAK) {
                        advance();
                    }
                } else if (currentToken.getType() == TokenType.DEFAULT) {
                    advance();
                    eat(TokenType.NEWLINE);
                    matchedWhenContent = parseBranchBlock();
                    if (currentToken.getType() == TokenType.BREAK) {
                        advance();
                    }
                } else {
                    if (currentToken.getType() == TokenType.NEWLINE) {
                        advance();
                    } else {
                        throw new ScriptSyntaxException("Unexpected token in choose statement: " + currentToken + ", At: " + currentTokenIndex);
                    }
                }
            }
            eat(TokenType.END);

            for (Map.Entry<String, List<Token>> entry : whenContentMap.entrySet()) {
                if (evaluateCondition(entry.getKey())) {
                    matchedWhenContent = entry.getValue();
                    break;
                }
            }
            if (matchedWhenContent.isEmpty()) {
                return "";
            }
            Parser parser = new Parser(matchedWhenContent, context);
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
            String listName = currentToken.getValue().substring(1);
            eat(TokenType.VARIABLE_NAME);

            String pipes = parsePipeLine();

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

            if (forContent.isEmpty()) {
                return "";
            }

            if (!open.isEmpty()) {
                open = open + NEW_LINE;
            }
            if (!close.isEmpty()) {
                close = NEW_LINE + close;
            }

            Object listObject = ObjectUtil.getDeepValue(context, listName);
            if (!isEmpty(pipes)) {
                listObject = expression("empty").pipedValue(listObject, pipes);
            }
            Object[] iterator = ObjectUtil.toArray(listObject);

            StringJoiner result = new StringJoiner(delimiter + '\n');
            Map<String, Object> localForVars = new HashMap<>();

            for (int i = 0, j = iterator.length; i < j; i++) {
                Object item = iterator[i];

                Map<String, Object> childContext = new HashMap<>(context);

                if (varName != null) {
                    localForVars.put(forVarKey(varName, forIndex, i), item);
                    childContext.put(varName, item);
                }
                if (idxName != null) {
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
         * @see IExpression
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
}