package com.github.chengyuxing.common.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 文件资源读取辅助工具类<br>
 * e.g. 文件格式
 * <blockquote>
 * <pre>windows: file:\\D:\\rabbit.sql</pre>
 * <pre>Linux/Unix: file:/root/rabbit.sql</pre>
 * <pre>ClassPath: sql/rabbit.sql</pre>
 * </blockquote>
 */
public class FileResource extends ClassPathResource {
    /**
     * 构造函数
     *
     * @param path 文件名
     */
    public FileResource(String path) {
        super(path);
    }

    /**
     * 文件名是否以file:开头
     *
     * @return 是否本地文件
     */
    private boolean isLocalFile() {
        return getPath().startsWith("file:");
    }

    /**
     * 获取文件输入流
     *
     * @return 输入流
     */
    @Override
    public InputStream getInputStream() {
        String path = getPath();
        if (isLocalFile()) {
            try {
                return new FileInputStream(path.substring(5));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
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
        if (isLocalFile()) {
            try {
                return new URL(getPath());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getURL();
    }
}
