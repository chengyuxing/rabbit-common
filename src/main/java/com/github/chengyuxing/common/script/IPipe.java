package com.github.chengyuxing.common.script;

/**
 * 值转换管道基础接口
 *
 * @param <T> 目标类型
 */
@FunctionalInterface
public interface IPipe<T> {
    T transform(Object value);

    /**
     * 获取为字符串长度
     */
    class Length implements IPipe<Integer> {

        @Override
        public Integer transform(Object value) {
            if (value == null) {
                return -1;
            }
            return value.toString().length();
        }
    }

    /**
     * 转为大写
     */
    class Upper implements IPipe<String> {

        @Override
        public String transform(Object value) {
            return value.toString().toUpperCase();
        }
    }

    /**
     * 转为小写
     */
    class Lower implements IPipe<String> {
        @Override
        public String transform(Object value) {
            return value.toString().toLowerCase();
        }
    }
}
