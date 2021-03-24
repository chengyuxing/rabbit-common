package rabbit.common.script;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rabbit.common.script.Comparators.compare;

/**
 * 条件表达式解析器<br>
 * 支持的逻辑运算符: {@code &&, ||}<br>
 * e.g.
 * <blockquote>
 * {@code !(:id >= 0 || :name <> blank) && :age<=21}
 * </blockquote>
 *
 * @see Comparators
 * @see FastExpression
 */
public class CExpression {
    private static final Pattern FILTER_PATTERN = Pattern.compile("\\s*:(?<name>\\w+)\\s*(?<op>[><=!@~]{1,2})\\s*(?<value>\\w+|'[^']*'|\"[^\"]*\"|-?[.\\d]+)\\s*");
    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("nashorn");
    private final String expression;

    /**
     * 构造函数
     *
     * @param expression 表达式
     */
    CExpression(String expression) {
        this.expression = expression;
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
     */
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
     */
    boolean calc(String expression, Map<String, Object> args) throws ScriptException {
        Matcher m = FILTER_PATTERN.matcher(expression);
        if (m.find()) {
            String filter = m.group(0);
            String name = m.group("name");
            String op = m.group("op");
            String value = m.group("value");
            boolean bool = compare(name, op, value, args);
            return calc(expression.replace(filter, bool + ""), args);
        }
        return Boolean.parseBoolean(SCRIPT_ENGINE.eval(expression).toString());
    }
}
