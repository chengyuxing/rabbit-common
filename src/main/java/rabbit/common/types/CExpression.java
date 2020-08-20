package rabbit.common.types;

import rabbit.common.tuple.Pair;
import rabbit.common.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 条件表达式解析器<br>
 * 支持比较的数据类型: {@code null, blank(空白字符串和null),true, false, 字符串(''或""), 数字}<br>
 * 支持的比较操作符: {@code >, <, >=, <=, == (=), != (<>)}<br>
 * 支持的逻辑运算符: {@code &&, ||}<br>
 * e.g.
 * <blockquote>
 * {@code :id >= 0 && :name <> blank && :age<=21}
 * </blockquote>
 */
public class CExpression {
    public static final String NUMBER_REGEX = "-?([0-9]|(0\\.\\d+)|([1-9]+\\.?\\d+))";
    private static final Pattern filterPattern = Pattern.compile(":(?<name>\\w+) *(?<op>[><=!]{1,2}) *(?<value>[\\S\\s]+)");

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
     * {@code :id >= 0 && :name <> blank && :age<=21}
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
     * @param params 参数字典
     * @return 逻辑运算的结果
     */
    public boolean getResult(Map<String, Object> params) {
        Pair<List<String>, List<String>> ps = StringUtil.split(expression, "(?<op>\\|\\||&&) *[^ '\"]", "op");

        List<String> filters = ps.getItem1();
        List<String> operators = ps.getItem2();

        List<Boolean> results = new ArrayList<>();
        for (String filter : filters) {
            Matcher m = filterPattern.matcher(filter.trim());
            if (m.matches()) {
                String name = m.group("name");
                String op = m.group("op");
                String value = m.group("value");
                boolean itemResult = compare(name, op, value, params);
                results.add(itemResult);
            } else {
                throw new IllegalArgumentException("child expression format error: " + filter);
            }
        }
        return boolCalc(results, operators);
    }

    /**
     * 一组布尔运算
     *
     * @param bools          一组布尔值
     * @param logicalSymbols 一组布尔值之间的逻辑连接符号
     * @return 运算后的布尔结果
     */
    public static boolean boolCalc(List<Boolean> bools, List<String> logicalSymbols) {
        if (bools.size() - logicalSymbols.size() != 1) {
            throw new IllegalArgumentException("logical error, bools size must greater than logicalSymbols size of 1");
        }
        if (bools.size() == 1) {
            return bools.get(0);
        }
        if (logicalSymbols.size() >= 1) {
            boolean prev = bools.get(0);
            boolean next = bools.get(1);
            String op = logicalSymbols.get(0);
            if (op.equals("&&")) {
                bools.remove(0);
                bools.set(0, prev && next);
            } else if (op.equals("||")) {
                bools.remove(0);
                bools.set(0, prev || next);
            }
            logicalSymbols.remove(0);
        }
        return boolCalc(bools, logicalSymbols);
    }

    /**
     * 值对比
     *
     * @param name   对应参数字典的key名
     * @param op     操作符
     * @param value  被比对的值
     * @param params 参数字典
     * @return 比较结果
     */
    private static boolean compare(String name, String op, String value, Map<String, Object> params) {
        Object source = params.get(name);
        if (op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")) {
            if (source != null && source.toString().matches(NUMBER_REGEX) && value.matches(NUMBER_REGEX)) {
                return compareNumber(name, op, value, params);
            } else {
                throw new UnsupportedOperationException(String.format("can not compare NonNumber: %s %s %s", name, op, value));
            }
        }
        return compareNonNumber(name, op, value, params);
    }

    /**
     * 对比数字类型
     *
     * @param name   对应参数字典的key名
     * @param op     操作符
     * @param value  被比对的值
     * @param params 参数字典
     * @return 比较结果
     */
    private static boolean compareNumber(String name, String op, String value, Map<String, Object> params) {
        double targetNum = Double.parseDouble(value);
        double sourceNum = Double.parseDouble(params.get(name).toString());
        switch (op) {
            case "=":
            case "==":
                return sourceNum == targetNum;
            case ">":
                return sourceNum > targetNum;
            case "<":
                return sourceNum < targetNum;
            case ">=":
                return sourceNum >= targetNum;
            case "<=":
                return sourceNum <= targetNum;
            case "!=":
            case "<>":
                return sourceNum != targetNum;
            default:
                throw new UnsupportedOperationException(String.format("unKnow operation of child expression: %s %s %s", name, op, value));
        }
    }

    /**
     * 对比非数字类型
     *
     * @param name   对应参数字典的key名
     * @param op     操作符
     * @param value  被比对的值
     * @param params 参数字典
     * @return 比较结果
     */
    private static boolean compareNonNumber(String name, String op, String value, Map<String, Object> params) {
        Object source = params.get(name);
        switch (op) {
            case "=":
            case "==":
                return equal(source, value);
            case "!=":
            case "<>":
                return !equal(source, value);
            default:
                throw new UnsupportedOperationException(String.format("can not compare NonNumber: \"%s\" %s %s", source, op, value));
        }
    }

    /**
     * 比对相等
     *
     * @param source 源值
     * @param value  被比对的值
     * @return 是否相等
     */
    private static boolean equal(Object source, String value) {
        if (value.equals("null")) {
            return source == null;
        }
        if (value.equals("blank")) {
            return source == null || "".equals(source.toString().trim());
        }
        if (value.equals("true")) {
            return source.equals(true);
        }
        if (value.equals("false")) {
            return source.equals(false);
        }
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1).equals(source);
        }
        if (source == null) {
            return false;
        }
        return value.equals(source.toString());
    }
}
