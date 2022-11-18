package com.github.chengyuxing.common.script.impl;

import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.script.IExpression;
import com.github.chengyuxing.common.script.IPipe;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h2>快速条件表达式解析器</h2>
 * 基于自行定制实现bool表达式脚本解析，功能较单一，但速度极快<br>
 * 支持的逻辑运算符: {@code &&, ||}<br>
 * e.g.
 * <blockquote>
 * <pre>!(:id{@code >=} 0 || :name | {@linkplain IPipe length}{@code <= 3) &&} :age{@code >} 21</pre>
 * </blockquote>
 *
 * @see Comparators
 */
public class FastExpression extends IExpression {
    private static final Map<String, IPipe<?>> GLOBAL_PIPES = new HashMap<>();
    private Map<String, IPipe<?>> customPipes = new HashMap<>();
    private static final Pattern FILTER_PATTERN = Pattern.compile("\\s*:(?<name>[\\w.]+)(?<pipes>(\\s*\\|\\s*\\w+)*)?\\s*(?<op>[><=!@~]{1,2})\\s*(?<value>\\w+|'[^']*'|\"[^\"]*\"|-?[.\\d]+)\\s*");
    private boolean checkArgsKey = true;

    static {
        GLOBAL_PIPES.put("length", new IPipe.Length());
        GLOBAL_PIPES.put("upper", new IPipe.Upper());
        GLOBAL_PIPES.put("lower", new IPipe.Lower());
    }

    /**
     * 构造函数
     *
     * @param expression 表达式
     */
    FastExpression(String expression) {
        super(expression);
    }

    /**
     * 创建一个表达式实例<br>
     * e.g.
     * <blockquote>
     * {@code !(:id >= 0 || :name <> blank) && :age<=21}
     * </blockquote>
     *
     * @param expression 表达式
     * @return 表达式实例
     */
    public static FastExpression of(String expression) {
        return new FastExpression(expression);
    }

    /**
     * 通过传入一个参数字典来获取解析表达式后进行逻辑运算的结果
     *
     * @param args 参数字典
     * @return 逻辑运算的结果
     * @throws IllegalArgumentException 如果设置了检查参数，参数中不存在的值进行计算则抛出错误
     * @throws ArithmeticException      如果表达式语法错误
     */
    @Override
    public boolean calc(Map<String, ?> args) {
        return calc(expression, args);
    }

    /**
     * 解析计算表达式
     *
     * @param expression 一组布尔值
     * @param args       参数字典
     * @return 运算后的布尔结果
     * @throws IllegalArgumentException 如果设置了检查参数，参数中不存在的值进行计算则抛出错误
     * @throws ArithmeticException      如果表达式语法错误
     * @throws NullPointerException     如果设置了检查参数，参数为null则抛出异常
     */
    boolean calc(String expression, Map<String, ?> args) {
        Matcher m = FILTER_PATTERN.matcher(expression);
        if (m.find()) {
            String filter = m.group(0);
            String name = m.group("name");
            String pipes = m.group("pipes");
            String op = m.group("op");
            String value = m.group("value");
            if (checkArgsKey) {
                if (args == null) {
                    throw new NullPointerException("args must not be null or field 'checkArgsKey' is true.");
                }
                boolean isKeyPath = name.contains(".") && !args.containsKey(name);
                String tk = name;
                if (isKeyPath) {
                    tk = name.substring(0, name.indexOf("."));
                }
                if (!args.containsKey(tk)) {
                    throw new IllegalArgumentException("value of key: '" + name + "' is not exists in " + args + " while calculate expression, or field 'checkArgsKey' is true.");
                }
            }
            Object source = ObjectUtil.getValueWild(args, name);
            if (pipes != null && !pipes.trim().equals("")) {
                try {
                    source = pipedValue(source, pipes);
                } catch (Exception e) {
                    throw new RuntimeException("an error occurred when piping value at expression -> " + expression, e);
                }
            }
            boolean bool = Comparators.compare(source, op, value);
            return calc(expression.replace(filter, Boolean.toString(bool)), args);
        }
        return boolExpressionEval(expression);
    }

    /**
     * 通过一系列管道来处理值
     *
     * @param value 值
     * @param pipes 管道 e.g.  <code>| {@linkplain IPipe upper} | {@linkplain IPipe length} | ...</code>
     * @return 经过管道处理后的值
     */
    public Object pipedValue(Object value, String pipes) {
        String trimPipes = pipes.trim();
        if (!trimPipes.matches("(\\s*\\|\\s*\\w+\\s*)+")) {
            throw new IllegalArgumentException("pipes channel syntax error: " + pipes);
        }
        String[] pipeArr = trimPipes.substring(1).split("\\|");
        Object res = value;
        for (String p : pipeArr) {
            String pipe = p.trim();
            if (customPipes.containsKey(pipe)) {
                res = customPipes.get(pipe).transform(res);
            } else if (GLOBAL_PIPES.containsKey(pipe)) {
                res = GLOBAL_PIPES.get(pipe).transform(res);
            } else {
                throw new RuntimeException("cannot find pipe '" + pipe + "'");
            }
        }
        return res;
    }

    /**
     * 设置是否检查参数是否存在
     *
     * @param checkArgsKey 检查参数
     */
    public void setCheckArgsKey(boolean checkArgsKey) {
        this.checkArgsKey = checkArgsKey;
    }

    /**
     * 配置自定义的管道
     *
     * @param customPipes 自定义管道字典
     */
    public void setCustomPipes(Map<String, IPipe<?>> customPipes) {
        this.customPipes = customPipes;
    }

    /**
     * 获取是否检查参数当前状态
     *
     * @return 是否检查
     */
    public boolean isCheckArgsKey() {
        return checkArgsKey;
    }

    /**
     * 布尔运算表达式动态解析计算<br>
     * e.g.
     * <blockquote>
     * {@code (true || false) && !(!(true && false || !!false)) || false}
     * </blockquote>
     *
     * @param expression 布尔表达式
     * @return true或false
     * @throws RuntimeException if expression syntax error.
     */
    public static boolean boolExpressionEval(String expression) {
        expression = expression.trim();
        // finally, get result
        if (expression.equals("true") || expression.equals("false")) {
            return Boolean.parseBoolean(expression);
        }
        char[] chars = expression.toCharArray();
        int start = -1;
        int end = -1;
        boolean lastStep = true;    // it means expression not contains '(,)' anymore, e.g. 'true && false || true || false'
        for (int i = chars.length - 1; i >= 0; i--) {
            char c = chars[i];
            if (c == ')') {
                end = i;
                lastStep = false;
                continue;
            }
            if (c == '(') {
                start = i;
                if (i > 0 && chars[i - 1] == '!') {
                    // it will be more than one '!' symbol like '!!!true'
                    while (i > 0) {
                        if (chars[i - 1] == '!') {
                            i--;
                        } else {
                            break;
                        }
                    }
                    start = i;
                }
                lastStep = false;
            }
            // if last step, it's not nest any sub expression
            // just get start to the end.
            if (i == 0 && lastStep) {
                start = 0;
                end = chars.length - 1;
            }
            // sub expression calc.
            if (end > -1 && start > -1) {
                // e.g. !!(true || false)
                String outerSub = expression.substring(start, end + 1);
                // 2 or more symbols of '!'
                int inverseCount = outerSub.lastIndexOf("!(") + 1;
                // if count of '!' is odd number, it means result must be inverse.
                boolean inverse = (inverseCount & 1) == 1;
                // except '(', ')' ,'!' symbols, e.g. 'true || false'
                String innerSub = expression.substring(start + inverseCount + 1, end);
                if (lastStep) {
                    innerSub = outerSub;
                }
                //start resolve bool value expression, e.g. 'true && false || true || false'
                Pair<List<String>, List<String>> s = StringUtil.regexSplit(innerSub, "(?<op>\\|\\||&&)", "op");
                List<String> values = s.getItem1();
                List<String> ops = s.getItem2();
                if (values.size() > 0) {
                    boolean res = false;
                    // it's look like '(true)'
                    if (ops.isEmpty()) {
                        res = parseBool(values.get(0));
                    } else {
                        for (int x = 0; x < ops.size(); x++) {
                            String op = ops.get(x).trim();
                            boolean currentValue = parseBool(values.get(x));
                            if (x == 0) {
                                // 'or' short calc
                                if (currentValue && op.equals("||")) {
                                    res = true;
                                    break;
                                }
                                // 'and' short calc，just break because 'false' is default
                                if (!currentValue && op.equals("&&")) {
                                    break;
                                }
                                // if short calc didn't match, make first var as result, then calc next step
                                res = currentValue;
                            }
                            boolean nextValue = parseBool(values.get(x + 1));
                            if (op.equals("||")) {
                                res = res || nextValue;
                            } else if (op.equals("&&")) {
                                res = res && nextValue;
                            }
                        }
                    }
                    // outer inverse calc
                    if (inverse) {
                        res = !res;
                    }
                    // replace the target sub expression result
                    // expression = expression.substring(0, start) + res + expression.substring(end + 1);
                    // if more than one same outer expression and result, just replace all, no need calc again!
                    expression = expression.replace(outerSub, res + "");
                    break;
                } else {
                    throw new ArithmeticException("expression syntax error:" + outerSub);
                }
            }
        }
        return boolExpressionEval(expression);
    }

    /**
     * 字符串类型bool值转换为bool基本数据类型
     *
     * @param bool 字符串类型bool值
     * @return bool基本数据类型
     */
    public static boolean parseBool(String bool) {
        String v = bool.trim();
        int inverseCount = v.lastIndexOf('!') + 1;
        boolean boolV = Boolean.parseBoolean(v.substring(inverseCount));
        if ((inverseCount & 1) == 1) {
            boolV = !boolV;
        }
        return boolV;
    }
}
