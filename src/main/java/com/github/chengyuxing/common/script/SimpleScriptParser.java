package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.impl.FastExpression;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.chengyuxing.common.script.Patterns.*;
import static com.github.chengyuxing.common.utils.ObjectUtil.*;
import static com.github.chengyuxing.common.utils.StringUtil.*;

/**
 * <h2>简单脚本解析器</h2>
 * <p>if语句块</p>
 * <blockquote>
 * <pre>
 * #if {@linkplain FastExpression expression}
 *      #if {@linkplain FastExpression expression}
 *      ...
 *      #fi
 * #fi
 * </pre>
 * </blockquote>
 * <p>choose语句块</p>
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
 * <p>switch语句块</p>
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
 * <p>for语句块</p>
 * <blockquote>
 * <pre>
 * #for item[,idx] of :list [| {@link IPipe pipe1} | pipeN | ... ] [delimiter ','] [open ''] [close '']
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
    private Map<String, Object> forVars = new HashMap<>();

    /**
     * 配置表达式解析器具体实现
     *
     * @param expression 当前解析过程中的表达式
     * @return 包含当前表达式的解析器
     */
    protected IExpression expression(String expression) {
        return FastExpression.of(expression);
    }

    /**
     * <code>#for</code> 循环体的模版内容格式化器，用于格式化自定义在循环体内的模版，例如：
     * <blockquote>
     * <pre>
     *     参数：<code>users: [{name:'cyx', name:'json'}]</code>
     *
     *     #for user of :users delimiter ' and '
     *        '${user.name}'
     *     #done
     * </pre>
     * 结果：<pre>'cyx' and 'json'</pre>
     * </blockquote>
     *
     * @param forIndex 每个for循环语句的序号
     * @param varIndex for变量的序号
     * @param varName  for变量名
     * @param idxName  for变量序号名
     * @param body     for循环里的内容主体
     * @param args     用户参数和for循环每次迭代的参数（序号和值）
     * @return 格式化后的内容
     */
    protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, List<String> body, Map<String, Object> args) {
        return FMT.format(String.join(NEW_LINE, body), args);
    }

    /**
     * 格式化表达式用以寻找前缀匹配 {@code #} 的表达式
     *
     * @param line 当前解析的内容行
     * @return 满足匹配表达式格式的字符串
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
     * 解析内容
     *
     * @param content 内容
     * @param data    逻辑表达式参数字典
     * @return 解析后的内容
     * @throws IllegalArgumentException 如果 {@code checkArgsKey} 为 {@code true} 并且 {@code args} 中不存在表达式所需要的key
     * @throws NullPointerException     如果 {@code args} 为null
     * @see IExpression
     */
    public String parse(String content, Map<String, ?> data) {
        if (Objects.isNull(content)) {
            return "";
        }
        forIndex = 0;
        forVars = new HashMap<>();
        return doParse(content, data == null ? new HashMap<>(0) : data);
    }

    /**
     * 递归执行解析内容
     *
     * @param content 内容
     * @param data    逻辑表达式参数字典
     * @return 解析后的内容
     * @throws IllegalArgumentException 如果 {@code checkArgsKey} 为 {@code true} 并且 {@code args} 中不存在表达式所需要的key
     * @throws NullPointerException     如果 {@code args} 为null
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
            int ifCount = 0;
            int forCount = 0;
            // 处理if表达式块
            if (startsWithIgnoreCase(expression, IF)) {
                ifCount++;
                StringJoiner buffer = new StringJoiner(NEW_LINE);
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithIgnoreCase(trimLine, IF)) {
                        buffer.add(line);
                        ifCount++;
                    } else if (startsWithIgnoreCase(trimLine, FI)) {
                        ifCount--;
                        if (ifCount < 0) {
                            throw new ScriptSyntaxException("can not find pair of '#if...#fi' block at line " + i);
                        }
                        // 说明此处已经达到了嵌套fi的末尾
                        if (ifCount == 0) {
                            // 此处计算外层if逻辑表达式，逻辑同程序语言的if逻辑
                            boolean res = expression(expression.substring(3)).calc(data);
                            // 如果外层判断为真，如果内层还有if或其他标签块，则进入内层继续处理
                            // 否则就认为是原始逻辑判断需要保留片段
                            if (res) {
                                output.add(doParse(buffer.toString(), data));
                            }
                            break;
                        } else {
                            // 说明此处没有达到外层fi，内层fi后面还有外层的内容需要保留
                            // e.g.
                            // #if
                            // ...
                            //      #if
                            //      ...
                            //      #fi
                            //      and t.a = :a    --此处为需要保留的地方
                            // #fi
                            buffer.add(line);
                        }
                    } else {
                        // 非if表达式的部分需要保留
                        buffer.add(line);
                    }
                }
                if (ifCount != 0) {
                    throw new ScriptSyntaxException("can not find pair of '#if...#fi' block at line " + i);
                }
            } else if (startsWithIgnoreCase(expression, CHOOSE)) {
                // 处理choose表达式块
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithsIgnoreCase(trimLine, WHEN, DEFAULT)) {
                        boolean res = false;
                        if (startsWithIgnoreCase(trimLine, WHEN)) {
                            res = expression(trimLine.substring(5)).calc(data);
                        }
                        // choose表达式块效果类似于程序语言的switch块，从前往后，只要满足一个分支，就跳出整个choose块
                        // 如果有default分支，前面所有when都不满足的情况下，就会直接选择default分支的内容作为结果保留
                        if (res || startsWithIgnoreCase(trimLine, DEFAULT)) {
                            StringJoiner buffer = new StringJoiner(NEW_LINE);
                            // 移动游标直到此分支的break之前都是符合判断结果保留下来
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), WHEN, DEFAULT)) {
                                    throw new ScriptSyntaxException("#when missing '#break' tag of expression '" + trimLine + "'");
                                }
                                buffer.add(lines[i]);
                            }
                            // when...break块中还可以嵌套
                            output.add(doParse(buffer.toString(), data));
                            // 到此处说明已经将满足条件的分支的内容保留下来
                            // 在接下来的分支都直接略过，移动游标直到end结束标签，就跳出整个choose块
                            //noinspection StatementWithEmptyBody
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), END)) ;
                            if (i == j) {
                                throw new ScriptSyntaxException("#choose missing '#end' close tag of choose expression block.");
                            }
                            break;
                        } else {
                            // 如果此分支when语句表达式不满足条件，就移动游标到当前分支break结束，进入下一个when分支
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), WHEN, DEFAULT)) {
                                    throw new ScriptSyntaxException("#choose missing '#break' tag of expression '" + trimLine + "'");
                                }
                            }
                        }
                    } else if (startsWithIgnoreCase(trimLine, END)) {
                        //在语句块为空的情况下，遇到end结尾，就跳出整个choose块
                        break;
                    } else {
                        output.add(line);
                    }
                }
            } else if (startsWithIgnoreCase(expression, SWITCH)) {
                // 处理switch表达式块，逻辑等同于choose表达式块
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
                            // case...break块中还可以嵌套
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
                // for表达式处理逻辑
                // item[,idx] of :list [| pipe1 | pipe2 | ... ] [delimiter ','] [open ''] [close '']
                forCount++;
                List<String> buffer = new ArrayList<>();
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithIgnoreCase(trimLine, FOR)) {
                        buffer.add(line);
                        forCount++;
                    } else if (startsWithIgnoreCase(trimLine, DONE)) {
                        forCount--;
                        if (forCount < 0) {
                            throw new ScriptSyntaxException("can not find pair of '#for...#done' block at line " + i);
                        }
                        if (forCount == 0) {
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
                                    // 每次都携带者for的迭代项和索引到参数字典中进行计算或为下一层提供参数
                                    Map<String, Object> eachArgs = new HashMap<>(data);
                                    // for循环迭代的变量保存下来提供给用户
                                    if (itemName != null) {
                                        localForVars.put(forVarKey(itemName, forIndex, x), value);
                                        eachArgs.put(itemName, value);
                                    }
                                    if (idxName != null) {
                                        localForVars.put(forVarKey(idxName, forIndex, x), x);
                                        eachArgs.put(idxName, x);
                                    }

                                    String formatted = forLoopBodyFormatter(forIndex, x, itemName, idxName, buffer, eachArgs);
                                    // 继续嵌套以解析其他标签语句
                                    String parsed = doParse(formatted, eachArgs);
                                    if (!parsed.trim().isEmpty()) {
                                        joiner.add(parsed);
                                    }
                                }
                                forIndex++;
                                String forBody = joiner.toString();
                                if (!forBody.trim().isEmpty()) {
                                    output.add(open + forBody + close);
                                    forVars.putAll(localForVars);
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
                if (forCount != 0) {
                    throw new ScriptSyntaxException("can not find pair of '#for...#done' block at line " + i);
                }
            } else {
                // 没有表达式的行，说明是需要保留的部分
                output.add(currentLine);
            }
        }
        return output.toString();
    }

    /**
     * for表达式变量的key名
     *
     * @param name   变量名
     * @param forIdx for序号
     * @param varIdx 变量序号
     * @return 变量key名
     */
    protected String forVarKey(String name, int forIdx, int varIdx) {
        return name + "_" + forIdx + "_" + varIdx;
    }

    /**
     * 获取for表达式执行过程中产生的临时变量
     *
     * @return for表达式变量字典
     */
    public Map<String, Object> getForVars() {
        return Collections.unmodifiableMap(forVars);
    }
}
