package com.github.chengyuxing.common.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 数据输出帮助通用接口
 */
public interface IOutput {
    /**
     * 输出到字节数据
     *
     * @return 字节数据
     * @throws IOException 存储字节发生移除
     */
    byte[] toBytes() throws IOException;

    /**
     * 写到输出流
     *
     * @param outputStream 输出流
     * @param close        是否在完成后关闭输出流
     * @throws IOException ioEx
     */
    default void saveTo(OutputStream outputStream, boolean close) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        out.write(toBytes());
        if (close) {
            out.flush();
            out.close();
        }
    }

    /**
     * 写到输出流并关闭输出流
     *
     * @param outputStream 输出流
     * @throws IOException ioEx
     */
    default void saveTo(OutputStream outputStream) throws IOException {
        saveTo(outputStream, true);
    }

    /**
     * 保存到指定路径下
     *
     * @param path 文件保存路径（后缀可选）
     * @throws IOException ioEx
     */
    default void saveTo(String path) throws IOException {
        saveTo(Files.newOutputStream(Paths.get(path)));
    }

    /**
     * 保存到文件对象
     *
     * @param file 文件对象
     * @throws IOException ioEx
     */
    default void saveTo(File file) throws IOException {
        saveTo(Files.newOutputStream(file.toPath()));
    }

    /**
     * 保存到路径对象
     *
     * @param path 路径对象
     * @throws IOException ioEx
     */
    default void saveTo(Path path) throws IOException {
        saveTo(Files.newOutputStream(path));
    }
}
