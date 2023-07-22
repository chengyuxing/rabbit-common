package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import com.github.chengyuxing.common.script.impl.FastExpression;
import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.chengyuxing.common.utils.StringUtil.*;

/**
 * <h2>简单脚本解析器</h2>
 * <p>if语句块</p>
 * <blockquote>
 * 支持嵌套if，choose，switch
 * <pre>
 * #if 表达式1
 *      #if 表达式2
 *      ...
 *      #fi
 * #fi
 * </pre>
 * </blockquote>
 * <p>choose语句块</p>
 * <blockquote>
 * 分支中还可以嵌套if语句
 * <pre>
 * #choose
 *      #when 表达式1
 *      ...
 *      #break
 *      #when 表达式2
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
 * 分支中还可以嵌套if语句
 * <pre>
 * #switch :变量 | {@linkplain IPipe 管道1} | {@linkplain IPipe 管道n} | ...
 *      #case 值1
 *      ...
 *      #break
 *      #case 值2
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
 * 内部不能嵌套其他任何标签，不进行解析
 * <pre>
 * #for item[,idx] of :list [| {@link IPipe pipe1} | pipe2 | ... ] [delimiter ','] [filter{@code $}{item.name}[| {@link IPipe pipe1} | pipe2 | ... ]{@code <>} blank]
 *     ...
 * #end
 * </pre>
 * </blockquote>
 *
 * @see IExpression
 */
public abstract class SimpleScriptParser {
    public static final Pattern FOR_PATTERN = Pattern.compile("(?<item>[\\w_]+)(\\s*,\\s*(?<index>[\\w_]+))?\\s+of\\s+:(?<list>[\\w_.]+)(?<pipes>(\\s*\\|\\s*[\\w_.]+)*)?(\\s+delimiter\\s+'(?<delimiter>[^']*)')?(\\s+filter\\s+(?<filter>[\\S\\s]+))?");
    public static final Pattern SWITCH_PATTERN = Pattern.compile(":(?<name>[\\w_.]+)\\s*(?<pipes>(\\s*\\|\\s*[\\w_.]+)*)?");
    public static final String[] TAGS = new String[]{
            "#if", "#fi",
            "#choose", "#when",
            "#switch", "#case",
            "#for",
            "#default", "#break",
            "#end"};
    public static final String IF = TAGS[0];
    public static final String FI = TAGS[1];
    public static final String CHOOSE = TAGS[2];
    public static final String WHEN = TAGS[3];
    public static final String SWITCH = TAGS[4];
    public static final String CASE = TAGS[5];
    public static final String FOR = TAGS[6];
    public static final String DEFAULT = TAGS[7];
    public static final String BREAK = TAGS[8];
    public static final String END = TAGS[9];

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
     *     #end
     * </pre>
     * 结果：<pre>'cyx' and 'json'</pre>
     * </blockquote>
     *
     * @param body for循环里的内容主体
     * @param args for循环每次迭代的参数（当前索引，当前值）
     * @return 格式化后的内容
     */
    protected abstract String forLoopBodyFormatter(String body, Map<String, Object> args);

    /**
     * 格式化表达式用以寻找前缀匹配 {@code #} 的表达式
     *
     * @param line 当前解析的内容行
     * @return 满足匹配表达式格式的字符串
     * @see #IF
     * @see #isExpression(String)
     */
    protected abstract String trimExpression(String line);

    /**
     * 判断当前行是否是动态sql的表达式
     *
     * @param line 当前解析的内容行
     * @return 是否是表达式
     * @see #trimExpression(String)
     */
    protected boolean isExpression(String line) {
        return false;
    }

    /**
     * 执行解析内容
     *
     * @param content      内容
     * @param args         逻辑表达式参数字典
     * @param checkArgsKey 检查参数中是否必须存在表达式中需要计算的key
     * @return 解析后的内容
     * @throws IllegalArgumentException 如果 {@code checkArgsKey} 为 {@code true} 并且 {@code args} 中不存在表达式所需要的key
     * @throws NullPointerException     如果 {@code args} 为null
     * @see IExpression
     */
    public String parse(String content, Map<String, ?> args, boolean checkArgsKey) {
        String[] lines = content.split(NEW_LINE);
        StringJoiner output = new StringJoiner(NEW_LINE);
        for (int i = 0, j = lines.length; i < j; i++) {
            String outerLine = lines[i];
            String trimOuterLine = trimExpression(outerLine);
            int count = 0;
            // 处理if表达式块
            if (startsWithIgnoreCase(trimOuterLine, IF)) {
                count++;
                StringJoiner innerSb = new StringJoiner(NEW_LINE);
                // 内循环推进游标，用来判断嵌套if表达式块
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithIgnoreCase(trimLine, IF)) {
                        innerSb.add(line);
                        count++;
                    } else if (startsWithIgnoreCase(trimLine, FI)) {
                        count--;
                        if (count < 0) {
                            throw new ScriptSyntaxException("can not find pair of 'if-fi' block at line " + i);
                        }
                        // 说明此处已经达到了嵌套fi的末尾
                        if (count == 0) {
                            // 此处计算外层if逻辑表达式，逻辑同程序语言的if逻辑
                            boolean res = expression(trimOuterLine.substring(3)).calc(args, checkArgsKey);
                            // 如果外层判断为真，如果内层还有if表达式块或choose...end块，则进入内层继续处理
                            // 否则就认为是原始sql逻辑判断需要保留片段
                            if (res) {
                                String innerStr = innerSb.toString();
                                if (containsAllIgnoreCase(innerStr, IF, FI) || containsAllIgnoreCase(innerStr, CHOOSE, END) || containsAllIgnoreCase(innerStr, SWITCH, END) || containsAllIgnoreCase(innerStr, FOR, END)) {
                                    output.add(parse(innerStr, args, checkArgsKey));
                                } else {
                                    output.add(innerStr);
                                }
                            }
                            break;
                        } else {
                            // 说明此处没有达到外层fi，内层fi后面还有外层的sql表达式需要保留
                            // e.g.
                            // #if
                            // ...
                            //      #if
                            //      ...
                            //      #fi
                            //      and t.a = :a    --此处为需要保留的地方
                            // #fi
                            innerSb.add(line);
                        }
                    } else {
                        // 非表达式的部分sql需要保留
                        innerSb.add(line);
                    }
                }
                if (count != 0) {
                    throw new ScriptSyntaxException("can not find pair of 'if-fi' block at line " + i);
                }
                // 处理choose表达式块
            } else if (startsWithIgnoreCase(trimOuterLine, CHOOSE)) {
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = trimExpression(line);
                    if (startsWithsIgnoreCase(trimLine, WHEN, DEFAULT)) {
                        boolean res = false;
                        if (startsWithIgnoreCase(trimLine, WHEN)) {
                            res = expression(trimLine.substring(5)).calc(args, checkArgsKey);
                        }
                        // choose表达式块效果类似于程序语言的switch块，从前往后，只要满足一个分支，就跳出整个choose块
                        // 如果有default分支，前面所有when都不满足的情况下，就会直接选择default分支的sql作为结果保留
                        if (res || startsWithIgnoreCase(trimLine, DEFAULT)) {
                            StringJoiner innerSb = new StringJoiner(NEW_LINE);
                            // 移动游标直到此分支的break之前都是符合判断结果的sql保留下来
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), WHEN, DEFAULT)) {
                                    throw new ScriptSyntaxException("missing '#break' tag of expression '" + trimLine + "'");
                                }
                                innerSb.add(lines[i]);
                            }
                            String innerStr = innerSb.toString();
                            // when...break块中还可以包含if表达式块
                            if (containsAllIgnoreCase(innerStr, IF, FI)) {
                                output.add(parse(innerStr, args, checkArgsKey));
                            } else {
                                output.add(innerStr);
                            }
                            // 到此处说明已经将满足条件的分支的sql保留下来
                            // 在接下来的分支都直接略过，移动游标直到end结束标签，就跳出整个choose块
                            //noinspection StatementWithEmptyBody
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), END)) ;
                            if (i == j) {
                                throw new ScriptSyntaxException("missing '#end' close tag of choose expression block.");
                            }
                            break;
                        } else {
                            // 如果此分支when语句表达式不满足条件，就移动游标到当前分支break结束，进入下一个when分支
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), WHEN, DEFAULT)) {
                                    throw new ScriptSyntaxException("missing '#break' tag of expression '" + trimLine + "'");
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
                // 处理switch表达式块，逻辑等同于choose表达式块
            } else if (startsWithIgnoreCase(trimOuterLine, SWITCH)) {
                Matcher m = SWITCH_PATTERN.matcher(trimOuterLine.substring(7));
                String name = null;
                String pipes = null;
                if (m.find()) {
                    name = m.group("name");
                    pipes = m.group("pipes");
                }
                if (name == null) {
                    throw new ScriptSyntaxException("switch syntax error of expression '" + trimOuterLine + "', cannot find var.");
                }
                Object value = ObjectUtil.getDeepValue(args, name);
                if (pipes != null && !pipes.trim().equals("")) {
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
                            StringJoiner innerSb = new StringJoiner(NEW_LINE);
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), CASE, DEFAULT)) {
                                    throw new ScriptSyntaxException("missing '#break' tag of expression '" + trimLine + "'");
                                }
                                innerSb.add(lines[i]);
                            }
                            String innerStr = innerSb.toString();
                            // case...break块中还可以包含if表达式块
                            if (containsAllIgnoreCase(innerStr, IF, FI)) {
                                output.add(parse(innerStr, args, checkArgsKey));
                            } else {
                                output.add(innerStr);
                            }
                            //noinspection StatementWithEmptyBody
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), END)) ;
                            if (i == j) {
                                throw new ScriptSyntaxException("missing '#end' close tag of switch expression block.");
                            }
                            break;
                        } else {
                            while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), BREAK)) {
                                if (startsWithsIgnoreCase(trimExpression(lines[i]), WHEN, DEFAULT)) {
                                    throw new ScriptSyntaxException("missing '#break' tag of expression '" + trimLine + "'");
                                }
                            }
                        }
                    } else if (startsWithIgnoreCase(trimLine, END)) {
                        break;
                    } else {
                        output.add(line);
                    }
                }
            } else if (startsWithIgnoreCase(trimOuterLine, FOR)) {
                Matcher m = FOR_PATTERN.matcher(trimOuterLine.substring(4).trim());
                if (m.find()) {
                    // 完整的表达式例如：item[,idx] of :list [| pipe1 | pipe2 | ... ] [delimiter ','] [filter ${item.name}[| pipe1 | pipe2 | ... ] <> blank]
                    // 方括号中为可选参数
                    String itemName = m.group("item");
                    String idxName = m.group("index");
                    String listName = m.group("list");
                    String pipes = m.group("pipes");
                    String delimiter = m.group("delimiter");
                    String filter = m.group("filter");
                    // 认为for表达式块中有多行需要迭代的sql片段，在此全部找出来用换行分割，保留格式
                    StringJoiner loopPart = new StringJoiner("\n");
                    while (++i < j && !startsWithIgnoreCase(trimExpression(lines[i]), END)) {
                        loopPart.add(lines[i]);
                    }
                    Object loopObj = ObjectUtil.getDeepValue(args, listName);
                    if (pipes != null && !pipes.trim().equals("")) {
                        loopObj = expression("empty").pipedValue(loopObj, pipes);
                    }
                    Object[] loopArr = ObjectUtil.toArray(loopObj);
                    // 如果没指定分割符，默认迭代sql片段最终使用逗号连接
                    StringJoiner forBody = new StringJoiner(delimiter == null ? ", " : delimiter.replace("\\n", "\n").replace("\\t", "\t"));
                    // 用于查找for定义变量的正则表达式
                    /// 需要验证下正则表达式 例如：user.address.street，超过2级
                    Pattern filterP = Pattern.compile("\\$\\{\\s*(?<tmp>(" + itemName + ")(.\\w+)*|" + idxName + ")\\s*}");
                    for (int x = 0; x < loopArr.length; x++) {
                        Map<String, Object> filterArgs = new HashMap<>();
                        filterArgs.put(itemName, loopArr[x]);
                        filterArgs.put(idxName, x);
                        // 如果定义了过滤器，首先对数据进行筛选操作，不满足条件的直接过滤
                        if (filter != null) {
                            // 查找过滤器中的引用变量
                            Matcher vx = filterP.matcher(filter);
                            Map<String, Object> filterTemps = new HashMap<>();
                            String expStr = filter;
                            while (vx.find()) {
                                String tmp = vx.group("tmp");
                                filterTemps.put(tmp, ":" + tmp);
                            }
                            // 将filter子句转为支持表达式解析的子句格式
                            expStr = FMT.format(expStr, filterTemps);
                            if (!expression(expStr).calc(filterArgs, checkArgsKey)) {
                                continue;
                            }
                        }
                        // 准备循环迭代生产满足条件的sql片段
                        String row = forLoopBodyFormatter(loopPart.toString().trim(), filterArgs);
                        forBody.add(row);
                    }
                    output.add(forBody.toString());
                } else {
                    throw new ScriptSyntaxException("for syntax error of expression '" + trimOuterLine + "' ");
                }
            } else {
                // 没有表达式的行，说明是原始sql的需要保留的部分
                output.add(outerLine);
            }
        }
        return output.toString();
    }
}
