package com.github.chengyuxing.common.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件资源读取辅助工具类<br>
 * e.g. 格式支持ClassPath下相对路径和URI格式
 * <blockquote>
 * <pre>windows: file:\\D:\\rabbit.sql 或 file:/D:/rabbit.sql</pre>
 * <pre>Linux/Unix: file:/root/rabbit.sql</pre>
 * <pre>ClassPath: sql/rabbit.sql</pre>
 * </blockquote>
 */
public class FileResource extends ClassPathResource {
    private final String uriOrClasspath;

    /**
     * 构造函数
     *
     * @param path 文件名
     */
    public FileResource(String path) {
        super(path);
        uriOrClasspath = path;
    }

    /**
     * 文件名是否以file:开头
     *
     * @return 是否本地文件
     */
    private boolean isURI() {
        return uriOrClasspath.startsWith("file:");
    }

    /**
     * 获取文件输入流
     *
     * @return 输入流
     */
    @Override
    public InputStream getInputStream() {
        if (isURI()) {
            try {
                return new FileInputStream(getURL().getFile());
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        }
        return super.getInputStream();
    }

    /**
     * 获取文件URL
     *
     * @return 文件URL
     */
    @Override
    public URL getURL() {
        if (isURI()) {
            try {
                URI uri = URI.create(uriOrClasspath);
                if (Files.exists(Paths.get(uri))) {
                    return uri.toURL();
                }
                return null;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getURL();
    }

    @Override
    public String getPath() {
        if (isURI()) {
            return getURL().getFile();
        }
        return super.getPath();
    }
}
