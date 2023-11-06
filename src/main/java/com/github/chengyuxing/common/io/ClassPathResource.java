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
 * Classpath resource.
 */
public class ClassPathResource {
    private final String path;
    private final ClassLoader classLoader;

    /**
     * Constructed a ClassPathResource with file path.
     *
     * @param path file path
     */
    public ClassPathResource(String path) {
        this.path = cleanPath(path);
        classLoader = getClassLoader();
    }

    /**
     * If path starts with '/', remove it.
     *
     * @param path path
     * @return correct classpath resource path
     */
    private String cleanPath(String path) {
        if (path.startsWith("/")) {
            return path.substring(1).trim();
        }
        return path.trim();
    }

    /**
     * Current resource path.
     *
     * @return 路径
     */
    public String getPath() {
        return path;
    }

    /**
     * Get current resource's input stream.
     *
     * @return current resource's input stream
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
     * Create a new BufferedReader of current resource.
     *
     * @param charset charset
     * @return BufferedReader
     */
    public BufferedReader getBufferedReader(Charset charset) {
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }

    /**
     * Read lines stream.
     *
     * @param charset charset
     * @return lines stream
     */
    public Stream<String> readLines(Charset charset) {
        return getBufferedReader(charset).lines();
    }

    /**
     * Read string content.
     *
     * @param charset charset
     * @return string content
     */
    public String readString(Charset charset) {
        try (Stream<String> lines = readLines(charset)) {
            return lines.collect(Collectors.joining("\n"));
        }
    }

    /**
     * Read bytes.
     *
     * @return bytes
     * @throws IOException if file not exists
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
     * Current resource transfer to another.
     *
     * @param out output stream
     * @throws IOException if file not exists
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
     * Check resource exists or not.
     *
     * @return true file exists or false
     */
    public boolean exists() {
        return getURL() != null;
    }

    /**
     * Get short file name.
     *
     * @return short file name
     */
    public String getFileName() {
        if (path.isEmpty()) {
            return null;
        }
        int index = path.lastIndexOf("/");
        return index != -1 ? path.substring(index + 1) : path;
    }

    /**
     * Get file name extension.
     *
     * @return file name extension
     */
    public String getFilenameExtension() {
        if (path.isEmpty()) {
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
     * Get current resource url.
     *
     * @return resource url
     */
    public URL getURL() {
        if (path.isEmpty()) {
            return null;
        }
        return classLoader != null ? classLoader.getResource(path) : ClassLoader.getSystemResource(path);
    }

    /**
     * Get resource last modified datetime.
     *
     * @return resource last modified datetime
     * @throws URISyntaxException if uri syntax error.
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
     * Class loader.
     *
     * @return Class loader
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
