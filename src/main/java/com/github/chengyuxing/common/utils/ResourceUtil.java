package com.github.chengyuxing.common.utils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Resource util.
 */
public final class ResourceUtil {
    /**
     * Get files by recursive.
     *
     * @param path       root path
     * @param depth      depth
     * @param suffix     file suffix
     * @param moreSuffix more file suffix
     * @return Stream
     * @throws IOException if path not exists
     */
    public static Stream<Path> getResources(Path path, int depth, String suffix, String... moreSuffix) throws IOException {
        return Files.find(path, depth, (p, attr) -> (StringUtil.endsWithIgnoreCase(p.toString(), suffix) || StringUtil.endsWithsIgnoreCase(p.toString(), moreSuffix)) && !attr.isDirectory());
    }

    /**
     * Get files by recursive.
     *
     * @param basePath   root path
     * @param suffix     file suffix
     * @param moreSuffix more file suffix
     * @return Stream
     * @throws IOException if path not exists
     */
    public static Stream<Path> getResources(Path basePath, String suffix, String... moreSuffix) throws IOException {
        return getResources(basePath, 5, suffix, moreSuffix);
    }

    public static String package2path(String packageName) {
        return packageName.replace('.', '/');
    }

    public static String path2package(String path) {
        return path.replace('/', '.');
    }

    /**
     * Check is file url or not by protocol.
     *
     * @param url url
     * @return true or false
     */
    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        return "file".equals(protocol) || "vfsfile".equals(protocol) || "vfs".equals(protocol);
    }

    /**
     * Check is jar url or not by protocol.
     *
     * @param url url
     * @return true or false
     */
    public static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        return "jar".equals(protocol) || "war".equals(protocol) || "zip".equals(protocol) || "vfszip".equals(protocol) || "wsjar".equals(protocol);
    }
}
