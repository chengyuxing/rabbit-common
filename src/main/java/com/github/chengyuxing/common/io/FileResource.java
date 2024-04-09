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
 * File resource, support classpath resource and local system file e.g.
 * <ul>
 * <li>Windows: {@code file:/D:/rabbit.sql}</li>
 * <li>Linux/Unix: {@code file:/root/rabbit.sql}</li>
 * <li>ClassPath: {@code sql/rabbit.sql}</li>
 * </ul>
 */
public class FileResource extends ClassPathResource {
    private final String uriOrClasspath;

    /**
     * Constructs a new FileResource with file path.
     *
     * @param path file path
     */
    public FileResource(String path) {
        super(path);
        uriOrClasspath = path.trim();
    }

    /**
     * Check resource is URI or not.
     *
     * @return true if is URI or false
     */
    private boolean isURI() {
        return uriOrClasspath.startsWith("file:");
    }

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
     * Get file extension.
     *
     * @param filename file name
     * @return file extension
     */
    public static String getFileExtension(String filename) {
        int dotIdx = filename.lastIndexOf(".");
        if (dotIdx != -1) {
            return filename.substring(dotIdx + 1);
        }
        return null;
    }

    /**
     * Get file short name.
     *
     * @param fullFileName  file name
     * @param withExtension with extension or not
     * @return file short name
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
     * Get string size view of bytes size.
     *
     * @param bytes bytes
     * @return string size view with unit
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
