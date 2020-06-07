package rabbit.common.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tab字符分割文件读取器
 */
public class TSVReader extends DSVReader {
    private final static String dl = "\t";

    /**
     * 构造函数
     *
     * @param in 输入流
     */
    TSVReader(InputStream in) {
        super(in);
    }

    /**
     * 指定输入流
     *
     * @param inputStream 输入流
     * @return 新实例
     */
    public static TSVReader of(InputStream inputStream) {
        return new TSVReader(inputStream);
    }

    /**
     * 指定文件路径
     *
     * @param path 路径
     * @return 新实例
     * @throws FileNotFoundException exp
     */
    public static TSVReader of(String path) throws FileNotFoundException {
        return of(new FileInputStream(path));
    }

    /**
     * 指定文件路径对象
     *
     * @param path 路径
     * @return 新实例
     * @throws IOException exp
     */
    public static TSVReader of(Path path) throws IOException {
        return of(Files.newInputStream(path));
    }

    /**
     * 指定文件对象
     *
     * @param file 文件对象
     * @return 新实例
     * @throws FileNotFoundException exp
     */
    public static TSVReader of(File file) throws FileNotFoundException {
        return of(new FileInputStream(file));
    }

    @Override
    protected String delimiter() {
        return dl;
    }
}
