package rabbit.common.io;

import rabbit.common.types.DataRow;
import rabbit.common.types.UncheckedCloseable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 抽象分隔符文件读取器
 */
public abstract class DSVReader {
    private final InputStream in;

    /**
     * 指定分隔符
     *
     * @return 分隔符 e.g. {@code "\t"}
     */
    protected abstract String delimiter();

    /**
     * 构造函数
     *
     * @param in 输入流
     */
    public DSVReader(InputStream in) {
        this.in = in;
    }

    /**
     * 返回一个流对象
     *
     * @return 流对象
     */
    public Stream<DataRow> stream() {
        UncheckedCloseable close;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        close = UncheckedCloseable.wrap(reader);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<DataRow>(Long.MAX_VALUE, Spliterator.ORDERED) {
            String[] names = null;
            String[] types = null;

            @Override
            public boolean tryAdvance(Consumer<? super DataRow> action) {
                try {
                    String line;
                    if ((line = reader.readLine()) == null) {
                        return false;
                    }
                    if (names == null) {
                        names = line.split(delimiter());
                        types = new String[names.length];
                        Arrays.fill(types, "java.lang.String");
                    }
                    action.accept(DataRow.of(names, types, line.split(delimiter())));
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, false).onClose(close);
    }
}
