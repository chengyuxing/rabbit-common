package com.github.chengyuxing.common.script.parser;

import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.script.expression.IExpression;
import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.*;
import java.util.regex.Matcher;

import static com.github.chengyuxing.common.utils.ObjectUtil.coalesce;
import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;
import static com.github.chengyuxing.common.utils.StringUtil.isEmpty;

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
            StringJoiner condition = new StringJoiner(" ");
            while (currentToken.getType() != TokenType.NEWLINE && currentToken.getType() != TokenType.EOF) {
                condition.add(currentToken.getValue());
                advance();
            }
            return condition.toString().trim();
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

        private String doParseStatement() {
            String result;
            switch (currentToken.getType()) {
                case IF:
                    result = parseIfStatement();
                    break;
                case SWITCH:
                    result = parseSwitchStatement();
                    break;
                case CHOOSE:
                    result = parseChooseStatement();
                    break;
                case FOR:
                    result = parseForStatement();
                    break;
                default:
                    result = currentToken.getValue();
                    advance();
                    break;
            }
            return result;
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
            String variable = parseCondition();
            eat(TokenType.NEWLINE);

            Map<String, List<Token>> caseContentMap = new LinkedHashMap<>();
            List<Token> matchedCaseContent = new ArrayList<>();

            while (currentToken.getType() != TokenType.END && currentToken.getType() != TokenType.EOF) {
                if (currentToken.getType() == TokenType.CASE) {
                    advance();
                    String caseValue = parseCondition();
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

            for (Map.Entry<String, List<Token>> entry : caseContentMap.entrySet()) {
                if (Comparators.compare(variableValue, "=", Comparators.valueOf(entry.getKey()))) {
                    matchedCaseContent = entry.getValue();
                    break;
                }
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
            Parser parser = new Parser(matchedWhenContent, context);
            return parser.doParse();
        }

        private String parseForStatement() {
            eat(TokenType.FOR);
            String forCondition = parseCondition();
            eat(TokenType.NEWLINE);
            List<Token> forContent = parseForBlock();
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

                    // for loop body content tokens.
                    List<Token> newForContent = new ArrayList<>(forContent.size());
                    for (Token token : forContent) {
                        if (token.getType() == TokenType.PLAIN_TEXT) {
                            String old = token.getValue();
                            String newValue = forLoopBodyFormatter(forIndex, i, itemName, idxName, old, childContext);
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
            } else {
                throw new ScriptSyntaxException("#for syntax error of expression '" + forCondition + "'" + ", At: " + currentTokenIndex);
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
