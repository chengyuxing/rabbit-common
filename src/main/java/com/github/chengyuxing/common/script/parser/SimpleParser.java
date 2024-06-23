package com.github.chengyuxing.common.script.parser;

import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.script.expression.IExpression;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.chengyuxing.common.script.lexer.FlowControlLexer.*;
import static com.github.chengyuxing.common.script.expression.Patterns.*;
import static com.github.chengyuxing.common.utils.ObjectUtil.*;
import static com.github.chengyuxing.common.utils.StringUtil.*;

/**
 * Based content line simple Flow-Control parser.
 */
public class SimpleParser extends AbstractParser {
    //language=RegExp
    public static final Pattern SWITCH_PATTERN = Pattern.compile(":(?<name>" + VAR_KEY_PATTERN + ")\\s*(?<pipes>" + PIPES_PATTERN + ")?");

    private int forIndex = 0;

    @Override
    public String parse(String content, Map<String, Object> context) {
        if (Objects.isNull(content)) {
            return "";
        }
        forIndex = 0;
        forContextVars = new HashMap<>();
        return doParse(content, context == null ? new HashMap<>(0) : context);
    }

    /**
     * Parse content with scripts.
     *
     * @param content content
     * @param data    data of expression
     * @return parsed content
     * @see IExpression
     */
    protected String doParse(String content, Map<String, ?> data) {
        if (content.trim().isEmpty()) {
            return content;
        }
        if (!containsAnyIgnoreCase(content, KEYWORDS)) {
            return content;
        }
        String[] lines = content.split(NEW_LINE);
        StringJoiner output = new StringJoiner(NEW_LINE);
        for (int i = 0, j = lines.length; i < j; i++) {
            String currentLine = lines[i];
            if (currentLine.trim().isEmpty()) {
                continue;
            }
            String expression = trimExpression(currentLine);
            int ifDepth = 0;
            int forDepth = 0;
            // if block
            if (startsWithIgnoreCase(expression, IF)) {
                ifDepth++;
                List<String> buffer = new ArrayList<>();
                buffer.add(currentLine);
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithIgnoreCase(trimLine, IF)) {
                        buffer.add(line);
                        ifDepth++;
                    } else if (startsWithIgnoreCase(trimLine, FI)) {
                        ifDepth--;
                        if (ifDepth < 0) {
                            throw new ScriptSyntaxException("can not find pair of '#if...#fi' block at line " + i);
                        }
                        // it means at the end of if block.
                        // this line means it's content need to hold.
                        // e.g.
                        // #if
                        // ...
                        //      #if
                        //      ...
                        //      #fi
                        //      and t.a = :a    --need to hold
                        // #fi
                        buffer.add(line);
                        if (ifDepth == 0) {
                            int depth = 0;
                            int elsePosition = -1;
                            for (int x = 0; x < buffer.size(); x++) {
                                String item = trimExpression(buffer.get(x));
                                if (startsWithIgnoreCase(item, IF)) {
                                    depth++;
                                } else if (startsWithIgnoreCase(item, FI)) {
                                    depth--;
                                } else if (startsWithIgnoreCase(item, ELSE) && depth == 1) {
                                    elsePosition = x;
                                }
                                if (depth == 0) {
                                    break;
                                }
                            }

                            List<String> thenContent;
                            List<String> elseContent = new ArrayList<>();
                            if (elsePosition == -1) {
                                thenContent = buffer.subList(1, buffer.size() - 1);
                            } else {
                                thenContent = buffer.subList(1, elsePosition);
                                elseContent = buffer.subList(elsePosition + 1, buffer.size() - 1);
                            }

                            List<String> res = expression(expression.substring(3)).calc(data) ? thenContent : elseContent;
                            String resContent = doParse(String.join("\n", res), data);
                            if (!resContent.trim().isEmpty()) {
                                output.add(resContent);
                            }
                            break;
                        }
                    } else {
                        // normal line need to hold.
                        buffer.add(line);
                    }
                }
                if (ifDepth != 0) {
                    throw new ScriptSyntaxException("can not find pair of '#if...#fi' block at line " + i);
                }
            } else if (startsWithIgnoreCase(expression, CHOOSE)) {
                // choose block
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithsIgnoreCase(trimLine, WHEN, DEFAULT)) {
                        boolean res = false;
                        if (startsWithIgnoreCase(trimLine, WHEN)) {
                            res = expression(trimLine.substring(5)).calc(data);
                        }
                        // if first case or default case passed.
                        if (res || startsWithIgnoreCase(trimLine, DEFAULT)) {
                            StringJoiner buffer = new StringJoiner(NEW_LINE);
                            // increment index and hold all lines until at break.
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), WHEN, DEFAULT)) {
                                    throw new ScriptSyntaxException("#when missing '#break' tag of expression '" + trimLine + "'");
                                }
                                buffer.add(lines[i]);
                            }
                            // do recursive to parse when...break block.
                            output.add(doParse(buffer.toString(), data));
                            // finish the when...block parse.
                            // break choose block.
                            //noinspection StatementWithEmptyBody
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), END)) ;
                            if (i == j) {
                                throw new ScriptSyntaxException("#choose missing '#end' close tag of choose expression block.");
                            }
                            break;
                        } else {
                            // if not pass, move to next case.
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), WHEN, DEFAULT)) {
                                    throw new ScriptSyntaxException("#choose missing '#break' tag of expression '" + trimLine + "'");
                                }
                            }
                        }
                    } else if (startsWithIgnoreCase(trimLine, END)) {
                        // break choose block until at end.
                        break;
                    } else {
                        output.add(line);
                    }
                }
            } else if (startsWithIgnoreCase(expression, SWITCH)) {
                // switch block logic like choose block.
                Matcher m = SWITCH_PATTERN.matcher(expression.substring(7));
                String name = null;
                String pipes = null;
                if (m.find()) {
                    name = m.group("name");
                    pipes = m.group("pipes");
                }
                if (name == null) {
                    throw new ScriptSyntaxException("#switch syntax error of expression '" + expression + "', cannot find var.");
                }
                Object value = getDeepValue(data, name);
                if (!isEmpty(pipes)) {
                    value = expression("empty").pipedValue(value, pipes);
                }
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithsIgnoreCase(trimLine, CASE, DEFAULT)) {
                        boolean res = false;
                        if (startsWithIgnoreCase(trimLine, CASE)) {
                            res = Comparators.compare(value, "=", Comparators.valueOf(trimLine.substring(5).trim()));
                        }
                        if (res || startsWithIgnoreCase(trimLine, DEFAULT)) {
                            StringJoiner buffer = new StringJoiner(NEW_LINE);
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), CASE, DEFAULT)) {
                                    throw new ScriptSyntaxException("#case missing '#break' tag of expression '" + trimLine + "'");
                                }
                                buffer.add(lines[i]);
                            }
                            output.add(doParse(buffer.toString(), data));
                            //noinspection StatementWithEmptyBody
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), END)) ;
                            if (i == j) {
                                throw new ScriptSyntaxException("#switch missing '#end' close tag of switch expression block.");
                            }
                            break;
                        } else {
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), WHEN, DEFAULT)) {
                                    throw new ScriptSyntaxException("#case missing '#break' tag of expression '" + trimLine + "'");
                                }
                            }
                        }
                    } else if (startsWithIgnoreCase(trimLine, END)) {
                        break;
                    } else {
                        output.add(line);
                    }
                }
            } else if (startsWithIgnoreCase(expression, FOR)) {
                // for expression block
                // item[,idx] of :list [| pipe1 | pipe2 | ... ] [delimiter ','] [open ''] [close '']
                forDepth++;
                StringJoiner buffer = new StringJoiner(NEW_LINE);
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithIgnoreCase(trimLine, FOR)) {
                        buffer.add(line);
                        forDepth++;
                    } else if (startsWithIgnoreCase(trimLine, DONE)) {
                        forDepth--;
                        if (forDepth < 0) {
                            throw new ScriptSyntaxException("can not find pair of '#for...#done' block at line " + i);
                        }
                        if (forDepth == 0) {
                            Matcher m = FOR_PATTERN.matcher(expression.substring(4).trim());
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

                                Object source = getDeepValue(data, listName);
                                if (!isEmpty(pipes)) {
                                    source = expression("empty").pipedValue(source, pipes);
                                }
                                Object[] iterator = toArray(source);

                                StringJoiner joiner = new StringJoiner(delimiter);
                                Map<String, Object> localForVars = new HashMap<>();
                                for (int x = 0, y = iterator.length; x < y; x++) {
                                    Object value = iterator[x];
                                    // create #for each temp args (name and index) for next inside expression block.
                                    Map<String, Object> eachArgs = new HashMap<>(data);
                                    // temp args save to local #for variable map for user.
                                    if (itemName != null) {
                                        localForVars.put(forVarKey(itemName, forIndex, x), value);
                                        eachArgs.put(itemName, value);
                                    }
                                    if (idxName != null) {
                                        localForVars.put(forVarKey(idxName, forIndex, x), x);
                                        eachArgs.put(idxName, x);
                                    }

                                    String formatted = forLoopBodyFormatter(forIndex, x, itemName, idxName, buffer.toString(), eachArgs);
                                    // keep do recursive to parse another inside expression.
                                    String parsed = doParse(formatted, eachArgs);
                                    if (!parsed.trim().isEmpty()) {
                                        joiner.add(parsed);
                                    }
                                }
                                forIndex++;
                                String forBody = joiner.toString();
                                if (!forBody.trim().isEmpty()) {
                                    output.add(open + forBody + close);
                                    forContextVars.putAll(localForVars);
                                }
                            } else {
                                throw new ScriptSyntaxException("#for syntax error of expression '" + expression + "'");
                            }
                            break;
                        } else {
                            buffer.add(line);
                        }
                    } else {
                        buffer.add(line);
                    }
                }
                if (forDepth != 0) {
                    throw new ScriptSyntaxException("can not find pair of '#for...#done' block at line " + i);
                }
            } else {
                // non-expression line need to hold.
                output.add(currentLine);
            }
        }
        return output.toString();
    }
}
