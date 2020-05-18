package rabbit.common.utils;

import java.util.Optional;

/**
 * 表达式
 */
public final class Exps {
    /**
     * 不限长赋值表达式<br>
     * 逻辑形如: if(a==b) return v1 else if(a==c) return v2 else if(a==d) return v3 (可选)全部匹配不到的默认值: else return v4
     *
     * @param value  值
     * @param equal  比较值
     * @param result 结果
     * @param more   更多
     * @param <T>    值类型
     * @param <R>    结果类型
     * @return 结果
     */
    @SuppressWarnings("unchecked")
    public static <T, R> R decode(T value, T equal, R result, Object... more) {
        Object[] objs = new Object[more.length + 2];
        objs[0] = equal;
        objs[1] = result;
        System.arraycopy(more, 0, objs, 2, more.length);
        boolean isOdd = objs.length % 2 != 0;
        R res = null;
        for (int i = 0; i < objs.length; i += 2) {
            if (value.equals(objs[i])) {
                if (i < objs.length - 1)
                    res = (R) objs[i + 1];
                break;
            }
            if (isOdd && i == objs.length - 1) {
                res = (R) objs[i];
                break;
            }
        }
        return res;
    }

    /**
     * 非空赋值表达式
     *
     * @param value 值
     * @param other 默认值
     * @param <T>   类型参数
     * @return 如果 value 为null 返回 other 否则返回 value
     */
    public static <T> T nullable(T value, T other) {
        return Optional.ofNullable(value).orElse(other);
    }
}
