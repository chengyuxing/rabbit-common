package com.github.chengyuxing.common.io;

import com.github.chengyuxing.common.utils.ResourceUtil;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            return path.substring(1).trim();
        }
        return path.trim();
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
     * 流式读取每一行（需要主动关闭流）
     *
     * @return 每一行的流对象
     */
    public Stream<String> readLines(Charset charset) {
        return new BufferedReader(new InputStreamReader(getInputStream(), charset)).lines();
    }

    /**
     * 读取为文本
     *
     * @return 文件文本内容
     */
    public String readString(Charset charset) {
        try (Stream<String> lines = readLines(charset)) {
            return lines.collect(Collectors.joining("\n"));
        }
    }

    /**
     * 读取为字节
     *
     * @return 文件字节
     * @throws IOException 如果文件读取失败
     */
    public byte[] readBytes() throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(getInputStream())) {
            byte[] bytes = new byte[inputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(bytes);
            return bytes;
        }
    }

    /**
     * 写入到输出流
     *
     * @param out 输出流
     * @throws IOException 如果文件读取失败
     */
    public void transferTo(OutputStream out) throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(getInputStream())) {
            byte[] buffer = new byte[4096];
            while (inputStream.read(buffer) != -1) {
                out.write(buffer);
            }
        }
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
        if (path.equals("")) {
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
        if (path.equals("")) {
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
        if (path.equals("")) {
            return null;
        }
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
