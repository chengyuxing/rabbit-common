package com.github.chengyuxing.common.io;

import com.github.chengyuxing.common.DataRow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * File resource, support classpath and uri e.g.
 * <ul>
 * <li>ClassPath: {@code sql/rabbit.sql}</li>
 * <li>Local File System:
 * <ul>
 *     <li>Windows: {@code file:/D:/rabbit.sql}</li>
 *     <li>Linux/Unix: {@code file:/root/rabbit.sql}</li>
 * </ul>
 * </li>
 * <li>HTTP(S): {@code http(s)://host/rabbit.sql}</li>
 * <li>FTP: {@code ftp://username:password@ftp.example.com/path/rabbit.sql}</li>
 * </ul>
 */
public class FileResource extends ClassPathResource {
    private final static String SCHEMAS_PATTERN = "(file|http|https|ftp)://.+";
    public static final String HTTP_PROP_CONNECTION_TIMEOUT = "connectTimeout";
    public static final String HTTP_PROP_READ_TIMEOUT = "readTimeout";
    public static final String HTTP_PROP_HEADERS = "headers";
    private final DataRow properties = new DataRow();
    private Function<String, InputStream> requestInterceptor;

    /**
     * Constructs a new FileResource with file path.
     *
     * @param path file path
     * @see FileResource
     */
    public FileResource(@NotNull String path) {
        super(path);
    }

    /**
     * Constructs a new FileResource with file path and buffer size.
     *
     * @param path       file path
     * @param bufferSize buffer size
     * @see FileResource
     */
    public FileResource(@NotNull String path, int bufferSize) {
        super(path, bufferSize);
    }

    /**
     * Check resource is URI or not.
     *
     * @return true if is URI or false
     */
    protected boolean isURI() {
        return path.matches(SCHEMAS_PATTERN) || path.startsWith("file:");
    }

    @Override
    public InputStream getInputStream() {
        if (requestInterceptor != null) {
            return requestInterceptor.apply(path);
        }
        Supplier<InputStream> interceptor = requestIntercept(path);
        if (interceptor != null) {
            return interceptor.get();
        }
        if (isURI()) {
            try {
                String schema = path.substring(0, path.indexOf(':'));
                switch (schema) {
                    case "file":
                        return Files.newInputStream(Paths.get(URI.create(path)));
                    case "http":
                    case "https":
                        HttpURLConnection httpCon = (HttpURLConnection) getURL().openConnection();
                        httpCon.setRequestMethod("GET");
                        if (properties.containsKey(HTTP_PROP_CONNECTION_TIMEOUT)) {
                            httpCon.setConnectTimeout(properties.getInt(HTTP_PROP_CONNECTION_TIMEOUT, 5000));
                        }
                        if (properties.containsKey(HTTP_PROP_READ_TIMEOUT)) {
                            httpCon.setReadTimeout(properties.getInt(HTTP_PROP_READ_TIMEOUT, 15000));
                        }
                        if (properties.containsKey(HTTP_PROP_HEADERS)) {
                            Map<String, Object> headers = properties.getAs(HTTP_PROP_HEADERS, new LinkedHashMap<>());
                            for (Map.Entry<String, Object> e : headers.entrySet()) {
                                httpCon.setRequestProperty(e.getKey(), e.getValue().toString());
                            }
                        }
                        return httpCon.getInputStream();
                    case "ftp":
                        URLConnection ftpCon = getURL().openConnection();
                        if (properties.containsKey(HTTP_PROP_CONNECTION_TIMEOUT)) {
                            ftpCon.setConnectTimeout(properties.getInt(HTTP_PROP_CONNECTION_TIMEOUT, 5000));
                        }
                        if (properties.containsKey(HTTP_PROP_READ_TIMEOUT)) {
                            ftpCon.setReadTimeout(properties.getInt(HTTP_PROP_READ_TIMEOUT, 15000));
                        }
                        return ftpCon.getInputStream();
                    default:
                        throw new UnsupportedOperationException("unknown schema");
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to open stream: " + getURL(), e);
            }
        }
        return super.getInputStream();
    }

    @Override
    public boolean exists() {
        if (path.startsWith("file:")) {
            return Files.exists(Paths.get(URI.create(path)));
        }
        return super.exists();
    }

    @Override
    public URL getURL() {
        if (isURI()) {
            try {
                return new URL(path);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getURL();
    }

    @Override
    public String getPath() {
        if (isURI()) {
            return getURL().getPath();
        }
        return super.getPath();
    }

    @Override
    public String getFilenameExtension() {
        if (isURI()) {
            return getFileExtension(getFileName(path, true));
        }
        return super.getFilenameExtension();
    }

    @Override
    public String getFileName() {
        if (isURI()) {
            return getFileName(path, true);
        }
        return super.getFileName();
    }

    /**
     * Intercept the resource request.
     */
    protected @Nullable Supplier<InputStream> requestIntercept(final String path) {
        return null;
    }

    /**
     * Set resource request interceptor.
     *
     * @param requestInterceptor request interceptor
     */
    public void setRequestInterceptor(Function<String, InputStream> requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }

    /**
     * Prepare some properties for resource if needed.
     *
     * @param properties properties e.g. http(s) request property {@code headers}:{@link Map}
     */
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    /**
     * Get file extension.
     *
     * @param filename file name
     * @return file extension
     */
    public static @Nullable String getFileExtension(@NotNull String filename) {
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
    public static @NotNull String getFileName(String fullFileName, boolean withExtension) {
        String name = fullFileName;
        if (name.matches(SCHEMAS_PATTERN)) {
            name = URI.create(name).getPath();
        }
        int index = name.lastIndexOf("/");
        name = index != -1 ? name.substring(index + 1) : name;
        if (withExtension || !name.contains(".")) {
            return name;
        }
        return name.substring(0, name.lastIndexOf("."));
    }

    /**
     * Get string size view of bytes size.
     *
     * @param bytes bytes length
     * @return string size view with unit
     */
    public static String formatFileSize(long bytes) {
        String size = "0 KB";
        final Formatter fmt = new Formatter();
        if (bytes > 1073741824) {
            size = fmt.format("%.2f", bytes / 1073741824.0) + " GB";
        } else if (bytes > 1048576) {
            size = fmt.format("%.2f", bytes / 1048576.0) + " MB";
        } else if (bytes > 0) {
            size = fmt.format("%.2f", bytes / 1024.0) + " KB";
        }
        return size;
    }
}
