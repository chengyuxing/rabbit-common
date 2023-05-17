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
 * 支持的逻辑运算符: {@code &&, ||, !}<br>
 * e.g.
 * <blockquote>
 * <pre>!(:id{@code >=} 0 || :name | {@link IPipe length}{@code <= 3) &&} :age{@code >} 21</pre>
 * </blockquote>
 *
 * @see Comparators
 */
public class FastExpression extends IExpression {
    private static final Map<String, IPipe<?>> GLOBAL_PIPES = new HashMap<>();
    private Map<String, IPipe<?>> customPipes = new HashMap<>();
    private static final Pattern CRITERIA_PATTERN = Pattern.compile("(?<compare>:?[\\w.-]+|'[^']*'|\"[^\"]*\")(?<comparePipes>(\\s*\\|\\s*\\w+)*)?\\s*(?<operator>[!=<>~@]{1,2})\\s*(?<compared>:?[\\w.-]+|'[^']*'|\"[^\"]*\")(?<comparedPipes>(\\s*\\|\\s*\\w+)*)?");

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
    public boolean calc(Map<String, ?> args, boolean require) {
        return calc(expression, args, require);
    }

    @Override
    public Map<String, IPipe<?>> pipes() {
        return customPipes;
    }

    /**
     * 解析计算表达式
     *
     * @param expression 一组布尔值
     * @param args       参数字典
     * @param require    参数是否为必须
     * @return 运算后的布尔结果
     * @throws IllegalArgumentException <ul>
     *                                  <li>如果 {@code require} 为 {@code true}，参数字典中不存在的值进行计算则抛出错误</li>
     *                                  <li>如果比较运算符不合法</li>
     *                                  </ul>
     * @throws ArithmeticException      如果表达式语法错误
     */
    boolean calc(String expression, Map<String, ?> args, boolean require) {
        Matcher m = CRITERIA_PATTERN.matcher(expression);
        if (m.find()) {
            String filter = m.group(0);
            String compare = m.group("compare");
            String comparePipes = m.group("comparePipes");
            String operator = m.group("operator");
            String compared = m.group("compared");
            String comparedPipes = m.group("comparedPipes");
            if (require) {
                validateArgs(compare, args);
                validateArgs(compared, args);
            }

            Object a = getValue(compare, comparePipes, args);
            Object b = getValue(compared, comparedPipes, args);

            boolean bool = Comparators.compare(a, operator, b);
            return calc(expression.replace(filter, Boolean.toString(bool)), args, require);
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
    private Object getValue(String name, String pipes, Map<String, ?> args) {
        Object value = name;
        if (isKey(name)) {
            value = ObjectUtil.getValueWild(args, name.substring(1));
        }
        if (pipes != null && !pipes.trim().equals("")) {
            try {
                value = pipedValue(value, pipes);
            } catch (Exception e) {
                throw new RuntimeException("an error occurred when piping value at expression -> " + expression, e);
            }
        }
        return Comparators.valueOf(value);
    }

    /**
     * 验证参数必须存在
     *
     * @param name 参数名
     * @param args 参数字典
     */
    private void validateArgs(String name, Map<String, ?> args) {
        if (isKey(name)) {
            String key = name.substring(1);
            if (args == null) {
                throw new NullPointerException("args must not be null or field 'checkArgsKey' is true.");
            }
            boolean isKeyPath = key.contains(".") && !args.containsKey(key);
            String tk = key;
            if (isKeyPath) {
                tk = key.substring(0, key.indexOf("."));
            }
            if (!args.containsKey(tk)) {
                throw new IllegalArgumentException("value of key: '" + key + "' is not exists in " + args + " while calculate expression, or field 'checkArgsKey' is true.");
            }
        }
    }

    /**
     * 传入的是键名还是字面量值
     *
     * @param a 字符串
     * @return 是否是键名
     */
    private boolean isKey(String a) {
        return a.startsWith(":");
    }

    /**
     * 通过一系列管道来处理值
     *
     * @param value 值
     * @param pipes 管道 e.g.  <code>| {@link IPipe upper} | {@link IPipe length} | ...</code>
     * @return 经过管道处理后的值
     * @throws IllegalArgumentException 管道语法错误
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
            if (pipes().containsKey(pipe)) {
                res = pipes().get(pipe).transform(res);
            } else if (GLOBAL_PIPES.containsKey(pipe)) {
                res = GLOBAL_PIPES.get(pipe).transform(res);
            } else {
                throw new RuntimeException("cannot find pipe '" + pipe + "'");
            }
        }
        return res;
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
