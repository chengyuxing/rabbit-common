package com.github.chengyuxing.common.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tab字符分割文件写入器
 */
public class TSVWriter extends DSVWriter {
    private static final String dl = "\t";

    TSVWriter(OutputStream out) {
        super(out);
    }

    /**
     * 输出到输出流
     *
     * @param outputStream 输出流
     * @return 新实例
     */
    public static TSVWriter of(OutputStream outputStream) {
        return new TSVWriter(outputStream);
    }

    /**
     * 指定输出路径
     *
     * @param path 路径
     * @return 新实例
     * @throws FileNotFoundException Exp
     */
    public static TSVWriter of(String path) throws FileNotFoundException {
        if (!path.endsWith(".tsv")) {
            path = path + ".tsv";
        }
        return of(new FileOutputStream(path));
    }

    /**
     * 指定输出路径
     *
     * @param path 路径对象
     * @return 新实例
     * @throws IOException ioExp
     */
    public static TSVWriter of(Path path) throws IOException {
        return of(Files.newOutputStream(path));
    }

    /**
     * 指定输出文件
     *
     * @param file 文件对象
     * @return 新实例
     * @throws FileNotFoundException ioExp
     */
    public static TSVWriter of(File file) throws FileNotFoundException {
        return of(new FileOutputStream(file));
    }

    @Override
    protected String delimiter() {
        return dl;
    }
}
