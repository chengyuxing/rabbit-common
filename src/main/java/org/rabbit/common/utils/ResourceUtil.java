package org.rabbit.common.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ResourceUtil {
    /**
     * 获取classPath下的文件
     *
     * @param path   路径
     * @param suffix 文件后缀
     * @return Stream
     * @throws IOException        没有此路径
     * @throws URISyntaxException 路径格式异常
     */
    public static Stream<Path> getClassPathResources(String path, String suffix) throws IOException, URISyntaxException {
        URL url = classLoader().getResource(path);
        Objects.requireNonNull(url);
        return Files.find(Paths.get(url.toURI()), 5, (p, attr) -> p.toString().endsWith(suffix) && !attr.isDirectory());
    }

    /**
     * 读取ClassPath下的文件到输入流
     *
     * @param file 文件
     * @return 输入流
     * @throws FileNotFoundException 没有找到文件异常
     */
    public static InputStream getClassPathResourceAsStream(final String file) throws FileNotFoundException {
        String _file = file;
        if (file.startsWith("/")) {
            _file = file.substring(1);
        }
        InputStream in = classLoader().getResourceAsStream(_file);
        return Optional.ofNullable(in)
                .orElseThrow(() -> new FileNotFoundException(file));
    }

    /**
     * 包名转换为路径
     *
     * @param packageName 包名
     * @return 路径
     */
    public static String package2path(String packageName) {
        return packageName.replace('.', '/');
    }

    /**
     * 路径转换为包名
     *
     * @param path 路径
     * @return 包名
     */
    public static String path2package(String path) {
        return path.replace('/', '.');
    }

    public static ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
