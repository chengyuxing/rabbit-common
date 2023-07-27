package com.github.chengyuxing.common.script.impl;

import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.script.IExpression;
import com.github.chengyuxing.common.script.IPipe;
import com.github.chengyuxing.common.script.exception.PipeNotFoundException;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.chengyuxing.common.script.Patterns.*;

/**
 * <h2>快速条件表达式解析器</h2>
 * 支持的逻辑运算符: {@code &&, ||, !}<br>
 * e.g.
 * <blockquote>
 * <pre>!(:id{@code >=} 0 || :name | {@link IPipe length}{@code <= 3) &&} :age{@code >} 21</pre>
 * </blockquote>
 *
 * @see Comparators
 */
public class FastExpression extends IExpression {
    //language=RegExp
    public static final Pattern CRITERIA_PATTERN = Pattern.compile("(?<compare>:?" + VAR_KEY_PATTERN + "|" + STRING_PATTERN + ")(?<comparePipes>" + PIPES_PATTERN + ")?\\s*(?<operator>[!=<>~@]{1,2})\\s*(?<compared>:?" + VAR_KEY_PATTERN + "|" + STRING_PATTERN + ")(?<comparedPipes>" + PIPES_PATTERN + ")?");
    private static final Map<String, IPipe<?>> GLOBAL_PIPES = new HashMap<>();
    private Map<String, IPipe<?>> customPipes = new HashMap<>();

    static {
        GLOBAL_PIPES.put("length", new IPipe.Length());
        GLOBAL_PIPES.put("upper", new IPipe.Upper());
        GLOBAL_PIPES.put("lower", new IPipe.Lower());
        GLOBAL_PIPES.put("pairs", new IPipe.Map2Pairs());
    }

    /**
     * 构造函数
     *
     * @param expression 表达式
     */
    public FastExpression(String expression) {
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

    @Override
    public boolean calc(Map<String, ?> args) {
        Objects.requireNonNull(args, expression + ": args must not be null.");
        return calc(expression, args);
    }

    /**
     * 设置自定义的管道字典
     *
     * @param pipes 管道字典实现
     */
    public void setPipes(Map<String, IPipe<?>> pipes) {
        if (pipes == null) {
            return;
        }
        if (this.customPipes.equals(pipes)) {
            return;
        }
        this.customPipes = new HashMap<>(pipes);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException 管道语法错误
     * @throws PipeNotFoundException    没有找到管道实现
     */
    @Override
    public Object pipedValue(Object value, String pipes) {
        String trimPipes = pipes.trim();
        if (trimPipes.isEmpty()) {
            return value;
        }
        if (!trimPipes.matches(PIPES_PATTERN)) {
            throw new IllegalArgumentException("pipes channel syntax error: " + pipes + " at expression -> " + expression);
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
                throw new PipeNotFoundException("cannot find pipe '" + pipe + "' at expression -> " + expression);
            }
        }
        return res;
    }

    /**
     * 解析计算表达式
     *
     * @param expression 一组布尔值
     * @param args       参数字典
     * @return 运算后的布尔结果
     * @throws IllegalArgumentException <ul>
     *                                  <li>如果 {@code require} 为 {@code true}，参数字典中不存在的值进行计算则抛出错误</li>
     *                                  <li>如果比较运算符不合法</li>
     *                                  </ul>
     * @throws ArithmeticException      如果表达式语法错误
     */
    boolean calc(String expression, Map<String, ?> args) {
        Matcher m = CRITERIA_PATTERN.matcher(expression);
        if (m.find()) {
            String criteria = m.group(0);
            String compare = m.group("compare");
            String comparePipes = m.group("comparePipes");
            String operator = m.group("operator");
            String compared = m.group("compared");
            String comparedPipes = m.group("comparedPipes");

            Object a = getValue(compare, comparePipes, args);
            Object b = getValue(compared, comparedPipes, args);

            boolean bool = Comparators.compare(a, operator, b);
            return calc(expression.replace(criteria, Boolean.toString(bool)), args);
        }
        return boolExpressionEval(expression);
    }

    /**
     * 获取参数值
     *
     * @param name  参数名或字面量值
     * @param pipes 参数值管道
     * @param args  参数字典
     * @return 可用于比较计算的值
     */
    protected Object getValue(String name, String pipes, Map<String, ?> args) {
        if (isKey(name)) {
            Object value = ObjectUtil.getDeepValue(args, name.substring(1));
            if (!StringUtil.isEmpty(pipes)) {
                value = pipedValue(value, pipes);
            }
            return value;
        }
        // 字符串字面量
        Object value = name;
        if (!StringUtil.isEmpty(pipes)) {
            value = pipedValue(value, pipes);
        }
        return Comparators.valueOf(value);
    }

    /**
     * 传入的是键名还是字面量值
     *
     * @param a 字符串
     * @return 是否是键名
     */
    protected boolean isKey(String a) {
        return a.startsWith(":");
    }

    /**
     * 布尔运算表达式动态解析计算<br>
     * e.g.
     * <blockquote>
     * {@code (true || false) && !(!(true && false || !!false)) || false}
     * </blockquote>
     *
     * @param expression 布尔表达式
     * @return true 或 false
     * @throws ArithmeticException 如果表达式语法错误
     */
    public static boolean boolExpressionEval(String expression) {
        expression = expression.trim();
        if (expression.equals("")) {
            return false;
        }
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
                    expression = expression.replace(outerSub, String.valueOf(res));
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
