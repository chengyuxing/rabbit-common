package com.github.chengyuxing.common.io;

import com.github.chengyuxing.common.UncheckedCloseable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 行基本类型数据输入输出帮助累
 */
public class Lines {
    /**
     * 读取行到一个流，(需要手动关闭)
     *
     * @param path      路径
     * @param delimiter 分隔符
     * @param charset   编码
     * @return 流数据
     * @throws IOException 如果路径错误
     * @see Stream#close()
     */
    public static Stream<List<String>> readLines(Path path, String delimiter, Charset charset) throws IOException {
        return Files.lines(path, charset).map(l -> Arrays.asList(l.split(delimiter)));
    }

    /**
     * 读取行到一个流，(需要手动关闭)
     *
     * @param path      路径
     * @param delimiter 分隔符
     * @return 流数据
     * @throws IOException 如果路径错误
     * @see Stream#close()
     */
    public static Stream<List<String>> readLines(Path path, String delimiter) throws IOException {
        return readLines(path, delimiter, StandardCharsets.UTF_8);
    }

    /**
     * 读取行到一个流，(需要手动关闭)
     *
     * @param in        输入流
     * @param delimiter 分隔符
     * @param charset   编码
     * @return 流数据
     * @throws RuntimeException 如果IO异常
     * @see Stream#close()
     */
    public static Stream<List<String>> readLines(InputStream in, String delimiter, Charset charset) {
        UncheckedCloseable close;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
        close = UncheckedCloseable.wrap(reader);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<List<String>>(Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(Consumer<? super List<String>> action) {
                try {
                    String line = reader.readLine();
                    if (line != null) {
                        action.accept(Arrays.asList(line.split(delimiter)));
                        return true;
                    }
                    return false;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, false).onClose(close);
    }

    /**
     * 读取行到一个流，(需要手动关闭)
     *
     * @param in        输入流
     * @param delimiter 分隔符
     * @return 流数据
     * @throws RuntimeException 如果IO异常
     * @see Stream#close()
     */
    public static Stream<List<String>> readLines(InputStream in, String delimiter) {
        return readLines(in, delimiter, StandardCharsets.UTF_8);
    }

    /**
     * 写入一行到输出流
     *
     * @param out       输出流
     * @param columns   列数据
     * @param delimiter 数据分隔符
     * @param <T>       类型参数
     * @throws IOException 如果IO异常
     */
    public static <T> void writeLine(OutputStream out, Collection<T> columns, String delimiter) throws IOException {
        String[] values = new String[columns.size()];
        int i = 0;
        for (T c : columns) {
            String v = c == null ? "" : c.toString();
            values[i] = v;
            i++;
        }
        String line = String.join(delimiter, values);
        out.write(line.getBytes());
        out.write("\n".getBytes());
    }

    /**
     * 写入多行到输出流
     *
     * @param out       输出流
     * @param rows      行数据
     * @param delimiter 数据分隔符
     * @param <T>       类型参数
     * @throws IOException 如果IO异常
     */
    public static <T> void writeLines(OutputStream out, Collection<Collection<T>> rows, String delimiter) throws IOException {
        for (Collection<T> row : rows) {
            writeLine(out, row, delimiter);
        }
    }
}
