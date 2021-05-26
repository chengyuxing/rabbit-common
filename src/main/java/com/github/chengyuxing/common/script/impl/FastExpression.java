package com.github.chengyuxing.common.script.impl;

import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.script.IExpression;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 快速条件表达式解析器<br>
 * 基于自行定制实现bool表达式脚本解析，功能较单一，但速度极快<br>
 * 支持的逻辑运算符: {@code &&, ||}<br>
 * e.g.
 * <blockquote>
 * {@code !(:id >= 0 || :name <> blank) && :age<=21}
 * </blockquote>
 *
 * @see Comparators
 */
public class FastExpression extends IExpression {
    private static final Pattern FILTER_PATTERN = Pattern.compile("\\s*:(?<name>\\w+)\\s*(?<op>[><=!@~]{1,2})\\s*(?<value>\\w+|'[^']*'|\"[^\"]*\"|-?[.\\d]+)\\s*");
    private boolean checkArgsKey = true;

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
    public boolean calc(Map<String, Object> args) {
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
    boolean calc(String expression, Map<String, Object> args) {
        Matcher m = FILTER_PATTERN.matcher(expression);
        if (m.find()) {
            String filter = m.group(0);
            String name = m.group("name");
            String op = m.group("op");
            String value = m.group("value");
            if (checkArgsKey) {
                if (args == null) {
                    throw new NullPointerException("args must not be null.");
                }
                if (!args.containsKey(name)) {
                    throw new IllegalArgumentException("value of key: '" + name + "' is not exists in " + args + " while calculate expression.");
                }
            }
            Object source = args == null ? null : args.get(name);
            boolean bool = Comparators.compare(source, op, value);
            return calc(expression.replace(filter, bool + ""), args);
        }
        return boolExpressionEval(expression);
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
        // finally get result
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
                boolean inverse = inverseCount % 2 == 1;
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
                            // 'or' short logic calc
                            if (x == 0 && currentValue && op.equals("||")) {
                                res = true;
                                break;
                            }
                            // 'and' short logic calc
                            if (x == 0 && !currentValue && op.equals("&&")) {
                                res = false;
                                break;
                            }
                            // else prepare the next calc
                            res = currentValue;
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
        if (inverseCount % 2 == 1) {
            boolV = !boolV;
        }
        return boolV;
    }
}
