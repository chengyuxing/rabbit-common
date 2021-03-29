package rabbit.common.script.impl;

import rabbit.common.script.Comparators;
import rabbit.common.script.IExpression;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rabbit.common.script.Comparators.compare;

/**
 * 条件表达式解析器<br>
 * 基于JDK提供的脚本解析引擎，功能较多，但解析速度较慢<br>
 * 支持的逻辑运算符: {@code &&, ||}<br>
 * e.g.
 * <blockquote>
 * {@code !(:id >= 0 || :name <> blank) && :age<=21}
 * </blockquote>
 *
 * @see Comparators
 * @see FastExpression
 */
public class CExpression extends IExpression {
    private static final Pattern FILTER_PATTERN = Pattern.compile("\\s*:(?<name>\\w+)\\s*(?<op>[><=!@~]{1,2})\\s*(?<value>\\w+|'[^']*'|\"[^\"]*\"|-?[.\\d]+)\\s*");
    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("nashorn");
    private boolean checkArgsKey;

    /**
     * 构造函数
     *
     * @param expression 表达式
     */
    CExpression(String expression) {
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
    public static CExpression of(String expression) {
        return new CExpression(expression);
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
        try {
            return calc(expression, args);
        } catch (ScriptException e) {
            throw new ArithmeticException("expression script syntax error：" + e.getMessage());
        }
    }

    /**
     * 解析计算表达式
     *
     * @param expression 一组布尔值
     * @param args       参数字典
     * @return 运算后的布尔结果
     * @throws IllegalArgumentException 如果设置了检查参数，参数中不存在的值进行计算则抛出错误
     */
    boolean calc(String expression, Map<String, Object> args) throws ScriptException {
        Matcher m = FILTER_PATTERN.matcher(expression);
        if (m.find()) {
            String filter = m.group(0);
            String name = m.group("name");
            String op = m.group("op");
            String value = m.group("value");
            if (checkArgsKey) {
                if (!args.containsKey(name)) {
                    throw new IllegalArgumentException("value of key: '" + name + "' is not exists in " + args + " while calculate expression.");
                }
            }
            boolean bool = compare(args.get(name), op, value);
            return calc(expression.replace(filter, bool + ""), args);
        }
        return Boolean.parseBoolean(SCRIPT_ENGINE.eval(expression).toString());
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
     * 设置是否检查参数是否存在
     *
     * @param checkArgsKey 检查参数
     */
    public void setCheckArgsKey(boolean checkArgsKey) {
        this.checkArgsKey = checkArgsKey;
    }
}
