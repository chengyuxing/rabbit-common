package com.github.chengyuxing.common.io;

import com.github.chengyuxing.common.utils.ResourceUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * 类路径资源读取工具类
 */
public class ClassPathResource {
    private final String path;
    private final ClassLoader classLoader;

    /**
     * 构造函数
     *
     * @param path 路径
     */
    public ClassPathResource(String path) {
        this.path = cleanPath(path);
        classLoader = getClassLoader();
    }

    /**
     * 清理路径
     *
     * @param path 路径
     * @return 合适的路径
     */
    private String cleanPath(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    /**
     * 获取当前资源的路径
     *
     * @return 路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取当前资源的输入流
     *
     * @return 资源文件输入流
     */
    public InputStream getInputStream() {
        InputStream in;
        if (classLoader != null) {
            in = classLoader.getResourceAsStream(path);
        } else {
            in = ClassLoader.getSystemResourceAsStream(path);
        }
        return in;
    }

    /**
     * 判断文件是否存在
     *
     * @return 文件是否存在
     */
    public boolean exists() {
        return getURL() != null;
    }

    /**
     * 获取文件名
     *
     * @return 文件名
     */
    public String getFileName() {
        if (path == null) {
            return null;
        }
        int index = path.lastIndexOf("/");
        return index != -1 ? path.substring(index + 1) : path;
    }

    /**
     * 获取文件名后缀
     *
     * @return 文件名后缀
     */
    public String getFilenameExtension() {
        if (path == null) {
            return null;
        } else {
            int extIndex = path.lastIndexOf(46);
            if (extIndex == -1) {
                return null;
            } else {
                int folderIndex = path.lastIndexOf("/");
                return folderIndex > extIndex ? null : path.substring(extIndex + 1);
            }
        }
    }

    /**
     * 获取当前资源的URL
     *
     * @return 资源URL
     */
    public URL getURL() {
        return classLoader != null ? classLoader.getResource(path) : ClassLoader.getSystemResource(path);
    }

    /**
     * 获取当前资源文件最后一个修改时间<br>
     * 如果是jar内的资源则无法正确获取，返回-1
     *
     * @return 文件最后一次修改时间
     * @throws URISyntaxException uri exp
     */
    public long getLastModified() throws URISyntaxException {
        URL url = getURL();
        long dt = 0;
        if (ResourceUtil.isFileURL(url)) {
            File f = Paths.get(url.toURI()).toFile();
            dt = f.lastModified();
        } else if (ResourceUtil.isJarURL(url)) {
            dt = -1;
        }
        return dt;
    }

    /**
     * 获取类启动器
     *
     * @return 类启动器
     */
    public static ClassLoader getClassLoader() {
        ClassLoader cl = null;

        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ignored) {
        }

        if (cl == null) {
            cl = ClassPathResource.class.getClassLoader();
            if (cl == null) {
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ignored) {
                }
            }
        }

        return cl;
    }
}
