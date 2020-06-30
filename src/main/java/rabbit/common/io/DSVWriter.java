package rabbit.common.io;

import rabbit.common.types.DataRow;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 抽象分隔符文件写入器
 */
public abstract class DSVWriter implements AutoCloseable {
    private final OutputStream out;
    private String header = null;

    /**
     * 分隔符 e.g. {@code "\t"}
     *
     * @return 分隔符
     */
    protected abstract String delimiter();

    /**
     * 构造函数
     *
     * @param out 输出流
     */
    public DSVWriter(OutputStream out) {
        this.out = out;
    }

    /**
     * 写入行
     *
     * @param columns 单元格
     * @param <T> 类型参数
     * @throws IOException ioExp
     */
    public <T> void writeLine(Collection<T> columns) throws IOException {
        String line = columns.stream().map(Object::toString).collect(Collectors.joining(delimiter()));
        out.write(line.getBytes());
        out.write("\n".getBytes());
    }

    /**
     * 写入行数据类型
     *
     * @param row 行
     * @throws IOException ioExp
     */
    public void writeLine(DataRow row) throws IOException {
        if (header == null) {
            header = String.join(delimiter(), row.getNames());
            writeLine(row.getNames());
        }
        writeLine(row.getValues());
    }

    /**
     * 关闭
     *
     * @throws Exception exp
     */
    @Override
    public void close() throws Exception {
        out.flush();
        out.close();
        header = null;
    }
}
