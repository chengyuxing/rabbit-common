package rabbit.common.types;

/**
 * 未检查异常的可关闭对象接口
 */
public interface UncheckedCloseable extends Runnable, AutoCloseable {
    @Override
    default void run() {
        try {
            close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 包裹一个可关闭对象
     *
     * @param closeable 　可关闭对象
     * @return 可关闭对象
     */
    static UncheckedCloseable wrap(AutoCloseable closeable) {
        return closeable::close;
    }

    /**
     * 嵌套可关闭的对象,从嵌套的最内层开始关闭<br>
     * 通过try-with-resource触发关闭对象，外层最后关闭
     *
     * @param closeable 可关闭对象
     * @return 嵌套的可关闭对象函数
     */
    default UncheckedCloseable nest(AutoCloseable closeable) {
        return () -> {
            try (UncheckedCloseable ignored = this) {
                closeable.close();
            }
        };
    }
}
