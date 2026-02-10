package com.github.chengyuxing.common.script.ast.impl;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.IExpr;
import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.lang.ForContextProperty;
import com.github.chengyuxing.common.script.lang.Token;
import com.github.chengyuxing.common.script.lang.TokenType;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.util.StringUtils;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.util.*;

import static com.github.chengyuxing.common.util.StringUtils.NEW_LINE;

public class RabbitScriptParser {
    private final List<Token> tokens;
    private int currentTokenIndex;
    private Token currentToken;

    public RabbitScriptParser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.currentToken = tokens.get(currentTokenIndex);
    }

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

    private boolean peek(TokenType type) {
        return currentToken.getType() == type;
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

    private boolean eof() {
        return currentToken.getType() == TokenType.EOF;
    }

    private IExpr<Boolean> parseCondition() {
        return parseOr();
    }

    private IExpr<Boolean> parseOr() {
        IExpr<Boolean> left = parseAnd();
        while (peek(TokenType.LOGIC_OR)) {
            String op = currentToken.getValue();
            advance();
            IExpr<Boolean> right = parseAnd();
            left = new LogicExpr(left, op, right);
        }
        return left;
    }

    private IExpr<Boolean> parseAnd() {
        IExpr<Boolean> left = parseCompare();
        while (peek(TokenType.LOGIC_AND)) {
            String op = currentToken.getValue();
            advance();
            IExpr<Boolean> right = parseCompare();
            left = new LogicExpr(left, op, right);
        }
        return left;
    }

    private IExpr<Boolean> parseCompare() {
        if (peek(TokenType.LPAREN)) {
            advance();
            IExpr<Boolean> expr = parseCondition();
            eat(TokenType.RPAREN);
            return expr;
        }
        if (peek(TokenType.LOGIC_NOT)) {
            advance();
            return new NotExpr(parseCompare());
        }

        ValueExpr a = parseValueWithPipes();

        if (!peek(TokenType.OPERATOR)) {
            return new UnaryExpr(a);
        }

        String op = currentToken.getValue();
        eat(TokenType.OPERATOR);

        ValueExpr b = parseValueWithPipes();

        return new BinaryExpr(a, op, b);
    }

    private List<String> parseVariableKeyExpression() {
        List<String> keys = new ArrayList<>();
        keys.add(currentToken.getValue());
        eat(TokenType.IDENTIFIER);
        while (nonEndToken(currentToken, TokenType.NEWLINE)) {
            if (peek(TokenType.DOT)) {
                advance();
                keys.add(currentToken.getValue());
                if (peek(TokenType.IDENTIFIER) || peek(TokenType.NUMBER)) {
                    advance();
                } else {
                    throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + " / " + TokenType.NUMBER);
                }
            } else if (peek(TokenType.LBRACKET)) {
                advance();
                if (!StringUtils.isNonNegativeInteger(currentToken.getValue())) {
                    throw new ScriptSyntaxException("Index must be a non-negative integer: " + currentToken.getValue());
                }
                keys.add(currentToken.getValue());
                advance();
                eat(TokenType.RBRACKET);
            } else {
                break;
            }
        }
        return keys;
    }

    private Object parseNumberLiteralValue(String sign) {
        String number = sign + currentToken.getValue();
        eat(TokenType.NUMBER);
        if (peek(TokenType.DOT)) {
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
            case "true":
            case "false":
                value = Boolean.parseBoolean(literal);
                break;
            case "null":
                value = null;
                break;
            case "blank":
                value = "";
                break;
            default:
                value = literal;
                break;
        }
        advance();
        return value;
    }

    private List<ValueExpr> parsePipeParams() {
        List<ValueExpr> params = new ArrayList<>();
        if (peek(TokenType.LPAREN)) {
            advance();
            while (nonEndToken(currentToken, TokenType.RPAREN, TokenType.NEWLINE)) {
                params.add(parseValue());
                if (peek(TokenType.COMMA)) {
                    advance();
                    if (peek(TokenType.RPAREN)) {
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

    private List<Pair<String, List<ValueExpr>>> parsePipes() {
        List<Pair<String, List<ValueExpr>>> pipes = new ArrayList<>();
        while (nonEndToken(currentToken, TokenType.NEWLINE)) {
            if (peek(TokenType.PIPE_SYMBOL)) {
                advance();
                String pipeName = currentToken.getValue();
                eat(TokenType.IDENTIFIER);
                List<ValueExpr> params = parsePipeParams();
                pipes.add(Pair.of(pipeName, params));
            } else {
                break;
            }
        }
        return pipes;
    }

    private ValueExpr parseValueWithPipes() {
        ValueExpr expr = parseValue();
        expr.setPipes(parsePipes());
        return expr;
    }

    private ValueExpr parseValue() {
        String literal = currentToken.getValue();
        ValueExpr value;
        switch (currentToken.getType()) {
            case IDENTIFIER:
                value = new ConstExpr(parseIdentifierLiteralValue());
                break;
            case STRING:
            case PLAIN_TEXT:
                value = new ConstExpr(literal);
                advance();
                break;
            case NUMBER:
                value = new ConstExpr(parseNumberLiteralValue("+"));
                break;
            case ADD_SYMBOL:
            case SUB_SYMBOL:
                String sign = currentToken.getValue();
                advance();
                switch (currentToken.getType()) {
                    case NUMBER:
                        value = new ConstExpr(parseNumberLiteralValue(sign));
                        break;
                    case IDENTIFIER:
                    case STRING:
                    case PLAIN_TEXT:
                        value = new ConstExpr(sign + literal);
                        advance();
                        break;
                    default:
                        throw new ScriptSyntaxException("Symbol '" + sign + "' cannot at the start of " + currentToken);
                }
                break;
            case COLON:
                advance();
                List<String> keys = parseVariableKeyExpression();
                value = new VarExpr(keys);
                break;
            default:
                throw new ScriptSyntaxException("Unexpected token: " + currentToken + ", expected: " + TokenType.IDENTIFIER + " / " + TokenType.STRING + " / " + TokenType.NUMBER + " / :<variable> e.g. user, user.name, books[0]");
        }
        return value;
    }

    private List<IElement> parseBlockUntil(TokenType... tokenTypes) {
        List<IElement> elements = new ArrayList<>();
        while (nonEndToken(currentToken, tokenTypes)) {
            elements.add(parseElement());
        }
        return elements;
    }

    private TextElement parseText() {
        StringJoiner joiner = new StringJoiner(NEW_LINE);
        while (peek(TokenType.PLAIN_TEXT)) {
            joiner.add(currentToken.getValue());
            advance();
        }
        return new TextElement(joiner.toString());
    }

    private IfElement parseIf() {
        eat(TokenType.IF);
        IExpr<Boolean> condition = parseCondition();
        eat(TokenType.NEWLINE);

        List<IElement> thenBlock = parseBlockUntil(TokenType.ELSE, TokenType.END_IF);
        List<IElement> elseBlock = new ArrayList<>();

        if (peek(TokenType.ELSE)) {
            advance();
            eat(TokenType.NEWLINE);
            elseBlock = parseBlockUntil(TokenType.END_IF);
        }
        eat(TokenType.END_IF);
        eat(TokenType.NEWLINE);
        return new IfElement(condition, thenBlock, elseBlock);
    }

    private CheckElement parseCheck() {
        eat(TokenType.CHECK);
        IExpr<Boolean> condition = parseCondition();
        eat(TokenType.CHECK_THROW);
        String message = currentToken.getValue();
        eat(TokenType.STRING);
        eat(TokenType.NEWLINE);
        return new CheckElement(condition, message);
    }

    private VarDefineElement parseVarDefine() {
        eat(TokenType.DEFINE_VAR);
        String name = currentToken.getValue();
        eat(TokenType.IDENTIFIER);
        if (peek(TokenType.OPERATOR) && currentToken.getValue().equals("=")) {
            advance();
            ValueExpr value = parseValueWithPipes();
            eat(TokenType.NEWLINE);
            return new VarDefineElement(name, value);
        }
        throw new ScriptSyntaxException("Unexcepted token: " + currentToken + ", excepted: '=' operator");
    }

    private GuardElement parseGuard() {
        eat(TokenType.GUARD);
        IExpr<Boolean> condition = parseCondition();
        eat(TokenType.NEWLINE);

        List<IElement> thenBlock = parseBlockUntil(TokenType.END_GUARD);

        eat(TokenType.END_GUARD);
        String message = currentToken.getValue();
        eat(TokenType.STRING);
        eat(TokenType.NEWLINE);

        return new GuardElement(condition, thenBlock, message);
    }

    private List<ValueExpr> parseSwitchCaseValues() {
        List<ValueExpr> values = new ArrayList<>();
        values.add(parseValueWithPipes());
        while (nonEndToken(currentToken, TokenType.NEWLINE)) {
            eat(TokenType.COMMA);
            values.add(parseValueWithPipes());
        }
        return values;
    }

    private SwitchElement parseSwitch() {
        eat(TokenType.SWITCH);
        ValueExpr source = parseValueWithPipes();
        eat(TokenType.NEWLINE);

        List<SwitchCaseBranchElement> cases = new ArrayList<>();
        BranchElement defaultBranch = new BranchElement();
        while (nonEndToken(currentToken, TokenType.END)) {
            if (peek(TokenType.CASE)) {
                advance();
                List<ValueExpr> values = parseSwitchCaseValues();
                eat(TokenType.NEWLINE);
                List<IElement> caseBlock = parseBlockUntil(TokenType.BREAK);
                eat(TokenType.BREAK);

                SwitchCaseBranchElement caseBranch = new SwitchCaseBranchElement(values, caseBlock);
                cases.add(caseBranch);
            } else if (peek(TokenType.DEFAULT)) {
                advance();
                eat(TokenType.NEWLINE);
                defaultBranch.setThenBlock(parseBlockUntil(TokenType.BREAK));
                eat(TokenType.BREAK);
            } else {
                eat(TokenType.NEWLINE);
            }
        }
        eat(TokenType.END);
        eat(TokenType.NEWLINE);
        return new SwitchElement(source, cases, defaultBranch);
    }

    private ChooseElement parseChoose() {
        eat(TokenType.CHOOSE);
        eat(TokenType.NEWLINE);

        List<ChooseWhenBranchElement> whens = new ArrayList<>();
        BranchElement defaultBranch = new BranchElement();
        while (nonEndToken(currentToken, TokenType.END)) {
            if (peek(TokenType.WHEN)) {
                advance();
                IExpr<Boolean> condition = parseCondition();
                eat(TokenType.NEWLINE);
                List<IElement> whenBlock = parseBlockUntil(TokenType.BREAK);
                eat(TokenType.BREAK);

                ChooseWhenBranchElement whenBranch = new ChooseWhenBranchElement(condition, whenBlock);
                whens.add(whenBranch);
            } else if (peek(TokenType.DEFAULT)) {
                advance();
                eat(TokenType.NEWLINE);
                defaultBranch.setThenBlock(parseBlockUntil(TokenType.BREAK));
                eat(TokenType.BREAK);
            } else {
                eat(TokenType.NEWLINE);
            }
        }
        eat(TokenType.END);
        eat(TokenType.NEWLINE);
        return new ChooseElement(whens, defaultBranch);
    }

    private void parseForProperty(String itemName, ForLoopElement forLoopElement) {
        Set<String> varNames = new HashSet<>();
        varNames.add(itemName);
        while (nonEndToken(currentToken, TokenType.NEWLINE)) {
            if (peek(TokenType.SEMICOLON)) {
                advance();
                if (peek(TokenType.IDENTIFIER)) {
                    Token propertyToken = currentToken;
                    String property = propertyToken.getValue(); // index
                    advance();
                    eat(TokenType.FOR_PROPERTY_AS);  // as
                    Token varToken = currentToken;
                    String varName = varToken.getValue();   // i
                    eat(TokenType.IDENTIFIER);

                    if (varNames.contains(varName)) {
                        throw new ScriptSyntaxException("Variable name '" + varName + "' already used in for statement at: " + varToken);
                    }
                    varNames.add(varName);

                    try {
                        switch (ForContextProperty.valueOf(property)) {
                            case index:
                                if (forLoopElement.getIndexName() != null) {
                                    throw new ScriptSyntaxException("Duplicate 'index' declaration in for statement at: " + varToken);
                                }
                                forLoopElement.setIndexName(varName);
                                break;
                            case first:
                                if (forLoopElement.getFirstName() != null) {
                                    throw new ScriptSyntaxException("Duplicate 'first' declaration in for statement at: " + varToken);
                                }
                                forLoopElement.setFirstName(varName);
                                break;
                            case last:
                                if (forLoopElement.getLastName() != null) {
                                    throw new ScriptSyntaxException("Duplicate 'last' declaration in for statement at: " + varToken);
                                }
                                forLoopElement.setLastName(varName);
                                break;
                            case odd:
                                if (forLoopElement.getOddName() != null) {
                                    throw new ScriptSyntaxException("Duplicate 'odd' declaration in for statement at: " + varToken);
                                }
                                forLoopElement.setOddName(varName);
                                break;
                            case even:
                                if (forLoopElement.getEvenName() != null) {
                                    throw new ScriptSyntaxException("Duplicate 'even' declaration in for statement at: " + varToken);
                                }
                                forLoopElement.setEvenName(varName);
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        throw new ScriptSyntaxException("Property '" + property + "' does not exist on for context at: " + propertyToken);
                    }
                }
            } else {
                break;
            }
        }
    }

    private ForLoopElement parseForLoop() {
        eat(TokenType.FOR);
        String itemName = currentToken.getValue();
        eat(TokenType.IDENTIFIER);
        eat(TokenType.FOR_OF);
        ValueExpr valueExpr = parseValueWithPipes();

        ForLoopElement forLoopElement = new ForLoopElement(itemName, valueExpr);

        parseForProperty(itemName, forLoopElement);

        eat(TokenType.NEWLINE);
        List<IElement> loopBody = parseBlockUntil(TokenType.END_FOR);
        forLoopElement.setLoopBlock(loopBody);

        eat(TokenType.END_FOR);
        eat(TokenType.NEWLINE);

        return forLoopElement;
    }

    private IElement parseElement() {
        switch (currentToken.getType()) {
            case IF:
                return parseIf();
            case CHECK:
                return parseCheck();
            case DEFINE_VAR:
                return parseVarDefine();
            case GUARD:
                return parseGuard();
            case SWITCH:
                return parseSwitch();
            case CHOOSE:
                return parseChoose();
            case FOR:
                return parseForLoop();
            case PLAIN_TEXT:
                return parseText();
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
                throw new ScriptSyntaxException("Impossible token: " + currentToken);
        }
    }

    public @Unmodifiable List<IElement> parse() {
        List<IElement> elements = new ArrayList<>();
        while (!eof()) {
            elements.add(parseElement());
        }
        return Collections.unmodifiableList(elements);
    }
}
