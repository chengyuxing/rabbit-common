package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.script.expression.IExpression;
import com.github.chengyuxing.common.script.expression.impl.FastExpression;
import com.github.chengyuxing.common.script.expression.IPipe;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.chengyuxing.common.script.expression.Patterns.*;
import static com.github.chengyuxing.common.utils.ObjectUtil.*;
import static com.github.chengyuxing.common.utils.StringUtil.*;

/**
 * <h2>Simple script parser</h2>
 * <p>if statement:</p>
 * <blockquote>
 * <pre>
 * #if {@linkplain FastExpression expression1}
 *      #if {@linkplain FastExpression expression2}
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
public class SimpleScriptParser {
    //language=RegExp
    public static final Pattern FOR_PATTERN = Pattern.compile("(?<item>\\w+)(\\s*,\\s*(?<index>\\w+))?\\s+of\\s+:(?<list>" + VAR_KEY_PATTERN + ")(?<pipes>" + PIPES_PATTERN + ")?(\\s+delimiter\\s+(?<delimiter>" + STRING_PATTERN + "))?(\\s+open\\s+(?<open>" + STRING_PATTERN + "))?(\\s+close\\s+(?<close>" + STRING_PATTERN + "))?");
    //language=RegExp
    public static final Pattern SWITCH_PATTERN = Pattern.compile(":(?<name>" + VAR_KEY_PATTERN + ")\\s*(?<pipes>" + PIPES_PATTERN + ")?");
    public static final String[] TAGS = new String[]{
            "#if", "#fi",
            "#choose", "#when",
            "#switch", "#case",
            "#default", "#break",
            "#end",
            "#for", "#done"};
    public static final String IF = TAGS[0];
    public static final String FI = TAGS[1];
    public static final String CHOOSE = TAGS[2];
    public static final String WHEN = TAGS[3];
    public static final String SWITCH = TAGS[4];
    public static final String CASE = TAGS[5];
    public static final String DEFAULT = TAGS[6];
    public static final String BREAK = TAGS[7];
    public static final String END = TAGS[8];
    public static final String FOR = TAGS[9];
    public static final String DONE = TAGS[10];

    private int forIndex = 0;
    private Map<String, Object> forContextVars = new HashMap<>();

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
     * @param args     each for loop args (index and value) which created by for expression
     * @return formatted content
     * @see #getForContextVars()
     */
    protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, List<String> body, Map<String, Object> args) {
        return FMT.format(String.join(NEW_LINE, body), args);
    }

    /**
     * Trim each line for search prefix {@code #} to detect expression.
     *
     * @param line current line
     * @return expression or normal line
     * @see #IF
     */
    protected String trimExpression(String line) {
        String tl = line.trim();
        if (tl.startsWith("#")) {
            return tl;
        }
        return line;
    }

    /**
     * Parse content with scripts.
     *
     * @param content content
     * @param data    data of expression
     * @return parsed content
     * @see IExpression
     */
    public String parse(String content, Map<String, ?> data) {
        if (Objects.isNull(content)) {
            return "";
        }
        forIndex = 0;
        forContextVars = new HashMap<>();
        return doParse(content, data == null ? new HashMap<>(0) : data);
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
        if (!containsAnyIgnoreCase(content, TAGS)) {
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
                StringJoiner buffer = new StringJoiner(NEW_LINE);
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
                        if (ifDepth == 0) {
                            boolean res = expression(expression.substring(3)).calc(data);
                            // if true do recursion to parse inside.
                            if (res) {
                                output.add(doParse(buffer.toString(), data));
                            }
                            break;
                        } else {
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
                List<String> buffer = new ArrayList<>();
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

                                    String formatted = forLoopBodyFormatter(forIndex, x, itemName, idxName, buffer, eachArgs);
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
}
