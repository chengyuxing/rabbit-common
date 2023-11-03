package com.github.chengyuxing.common.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Formatter;

/**
 * 文件资源读取辅助工具类<br>
 * e.g. 格式支持ClassPath下相对路径和URI格式
 * <blockquote>
 * <pre>windows: file:/D:/rabbit.sql</pre>
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
        uriOrClasspath = path.trim();
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
                Path p = Paths.get(uri);
                if (!Files.isDirectory(p) && Files.exists(p)) {
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

    /**
     * 获取文件名后缀
     *
     * @param filename 文件名
     * @return 文件后缀
     */
    public static String getFileExtension(String filename) {
        int dotIdx = filename.lastIndexOf(".");
        if (dotIdx != -1) {
            return filename.substring(dotIdx + 1);
        }
        return null;
    }

    /**
     * 根据路径获取文件名
     *
     * @param fullFileName  文件全路径名
     * @param withExtension 是否包含文件后缀
     * @return 文件名
     */
    public static String getFileName(String fullFileName, boolean withExtension) {
        String name;
        if (fullFileName.startsWith("file:")) {
            name = Paths.get(URI.create(fullFileName)).getFileName().toString();
        } else {
            name = Paths.get(fullFileName).getFileName().toString();
        }
        if (withExtension || !name.contains(".")) {
            return name;
        }
        return name.substring(0, name.lastIndexOf("."));
    }

    /**
     * 获取字节数组对象的大小
     *
     * @param bytes 字节数组
     * @return 文件大小
     */
    public static String getSize(byte[] bytes) {
        String strSize = "0KB";
        final Formatter fmt = new Formatter();
        if (bytes.length > 1073741824) {
            strSize = fmt.format("%.2f", bytes.length / 1073741824.0) + "GB";
        } else if (bytes.length > 1048576) {
            strSize = fmt.format("%.2f", bytes.length / 1048576.0) + "MB";
        } else if (bytes.length > 0) {
            strSize = fmt.format("%.2f", bytes.length / 1024.0) + "KB";
        }
        return strSize;
    }
}
