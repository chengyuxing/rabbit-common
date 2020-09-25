package rabbit.common.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 资源工具类
 */
public class ResourceUtil {
    /**
     * 递归获取指定目录下的文件
     *
     * @param path   基本路径
     * @param depth  深度
     * @param suffix 文件后缀
     * @return Stream
     * @throws IOException 没有此路径
     */
    public static Stream<Path> getResources(Path path, int depth, String suffix) throws IOException {
        return Files.find(path, depth, (p, attr) -> p.toString().endsWith(suffix) && !attr.isDirectory());
    }

    /**
     * 递归获取指定目录下的文件深度为5
     *
     * @param basePath 基本路径
     * @param suffix   文件后缀
     * @return Stream
     * @throws IOException        没有此路径
     * @throws URISyntaxException 路径格式异常
     */
    public static Stream<Path> getResources(Path basePath, String suffix) throws IOException, URISyntaxException {
        return getResources(basePath, 5, suffix);
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

    /**
     * 判断是否是一个文件系统协议
     *
     * @param url url
     * @return 是否是文件协议
     */
    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        return "file".equals(protocol) || "vfsfile".equals(protocol) || "vfs".equals(protocol);
    }

    /**
     * 判断路径是否是jar压缩包协议
     *
     * @param url url
     * @return 是否是jar压缩包协议
     */
    public static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        return "jar".equals(protocol) || "war".equals(protocol) || "zip".equals(protocol) || "vfszip".equals(protocol) || "wsjar".equals(protocol);
    }
}
