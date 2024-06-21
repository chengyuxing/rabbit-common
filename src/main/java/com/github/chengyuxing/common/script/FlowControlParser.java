package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.IExpression;
import com.github.chengyuxing.common.script.expression.impl.FastExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class FlowControlParser {
    private final List<Token> tokens;
    private int currentTokenIndex;
    private Token currentToken;
    private final Map<String, Object> context;

    public FlowControlParser(List<Token> tokens, Map<String, Object> context) {
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
        IExpression expression = FastExpression.of(condition);
        return expression.calc(context);
    }

    private String parseCondition() {
        StringBuilder condition = new StringBuilder();
        while (currentToken.getType() != TokenType.NEWLINE && currentToken.getType() != TokenType.EOF) {
            condition.append(currentToken.getValue()).append(" ");
            advance();
        }
        return condition.toString().trim();
    }

    private String parseForBlock() {
        StringBuilder condition = new StringBuilder();
        while (currentToken.getType() != TokenType.END_FOR) {
            condition.append(currentToken.getValue()).append(" ");
            advance();
        }
        return condition.toString().trim();
    }

    private void doParseStatement(StringBuilder result) {
        if (currentToken.getType() == TokenType.IF) {
            result.append(parseIfStatement()).append(" ");
        } else if (currentToken.getType() == TokenType.SWITCH) {
            result.append(parseSwitchStatement()).append(" ");
        } else if (currentToken.getType() == TokenType.CHOOSE) {
            result.append(parseChooseStatement()).append(" ");
        } else if (currentToken.getType() == TokenType.FOR) {
            result.append(parseForStatement()).append(" ");
        } else {
            result.append(currentToken.getValue()).append(" ");
            advance();
        }
    }

    private String parseContent() {
        StringBuilder content = new StringBuilder();
        while (currentToken.getType() != TokenType.ELSE &&
                currentToken.getType() != TokenType.ENDIF &&
                currentToken.getType() != TokenType.END_FOR &&
                currentToken.getType() != TokenType.BREAK &&
                currentToken.getType() != TokenType.END &&
                currentToken.getType() != TokenType.EOF) {
            doParseStatement(content);
        }
        return content.toString().trim();
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
        String variable = currentToken.getValue();
        eat(TokenType.VARIABLE_NAME);
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

        Object variableValue = context.get(variable.substring(1));
        if (variableValue != null) {
            String content = caseContentMap.get(variableValue.toString());
            if (content != null) {
                return content;
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
            delimiter = currentToken.getValue();
            advance();
        }
        String open = "";
        if (currentToken.getType() == TokenType.FOR_OPEN) {
            advance();
            open = currentToken.getValue();
            advance();
        }
        String close = "";
        if (currentToken.getType() == TokenType.FOR_CLOSE) {
            advance();
            close = currentToken.getValue();
            advance();
        }

        eat(TokenType.NEWLINE);
        String forContent = parseForBlock();
        eat(TokenType.END_FOR);
        Object listObject = context.get(listVariable.substring(1));
        if (listObject instanceof List) {
            StringJoiner result = new StringJoiner('\n' + delimiter + '\n', open, close);
            for (Object item : (List<?>) listObject) {
                context.put(itemVariable, item);
                FlowControlParser parser = new FlowControlParser(new FlowControlLexer(forContent).tokenize(), context);
                String forContentResult = parser.parse();
                if (!forContentResult.trim().isEmpty()) {
                    result.add(forContentResult);
                }
            }
            return result.toString().trim();
        } else {
            throw new ScriptSyntaxException("Variable " + listVariable + " is not a list.");
        }
    }

    public String parse() {
        StringBuilder result = new StringBuilder();
        while (currentToken.getType() != TokenType.EOF) {
            doParseStatement(result);
        }
        return result.toString().trim();
    }
}
