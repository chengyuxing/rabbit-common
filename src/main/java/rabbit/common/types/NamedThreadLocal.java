package rabbit.common.types;

/**
 * 命名的线程变量
 *
 * @param <T> 类型参数
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {
    private final String name;

    /**
     * 构造函数
     *
     * @param name 名称
     */
    public NamedThreadLocal(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
