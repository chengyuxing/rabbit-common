package com.github.chengyuxing.common.script;

import java.util.Map;

/**
 * 抽象bool表达式通用接口
 */
public abstract class IExpression {

    protected final String expression;

    protected IExpression(String expression) {
        this.expression = expression;
    }

    /**
     * 通过传入一个参数字典来获取解析表达式后进行逻辑运算的结果
     *
     * @param args    参数字典
     * @param require 参数是否为必须
     * @return 逻辑运算的结果
     * @throws IllegalArgumentException 如果 {@code require} 为 {@code true}，参数字典中不存在的值进行计算则抛出错误
     * @throws ArithmeticException      如果表达式语法错误
     */
    public abstract boolean calc(Map<String, ?> args, boolean require);

    /**
     * 用于扩展值处理的管道
     *
     * @return 管道字典
     * @see IPipe
     */
    public abstract Map<String, IPipe<?>> pipes();

    @Override
    public String toString() {
        return expression;
    }
}
