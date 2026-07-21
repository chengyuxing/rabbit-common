package com.github.chengyuxing.common.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

/**
 * File resource, support classpath and uri e.g.
 * <ul>
 * <li>ClassPath:
 * <ul>
 *     <li>{@code sql/rabbit.sql}</li>
 * </ul>
 * </li>
 * <li>Local File System ({@code file:/} or {@code file:///}):
 * <ul>
 *     <li>Windows: {@code file:/D:/rabbit.sql}</li>
 *     <li>Linux/Unix: {@code file:/root/rabbit.sql}</li>
 * </ul>
 * </li>
 * <li>Remote:
 * <ul>
 *     <li>HTTP(S): {@code http(s)://host/rabbit.sql}</li>
 *     <li>FTP: {@code ftp://username:password@ftp.example.com/path/rabbit.sql}</li>
 * </ul>
 * </li>
 * </ul>
 */
public class FileResource extends ClassPathResource {
    private final URI uri;
    private ConnectionInterceptor<HttpURLConnection> httpInterceptor = new ConnectionInterceptor<HttpURLConnection>() {
    };
    private ConnectionInterceptor<URLConnection> ftpInterceptor = new ConnectionInterceptor<URLConnection>() {
    };

    /**
     * Constructs a new FileResource with file path.
     *
     * @param path file path
     * @see FileResource
     */
    public FileResource(@NotNull String path) {
        super(path);
        this.uri = URI.create(path);
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
        this.uri = URI.create(path);
    }

    @Override
    public InputStream getInputStream() {
        Supplier<InputStream> supplier = resourceIntercept(path);
        if (supplier != null) {
            return supplier.get();
        }
        try {
            if (!uri.isAbsolute()) {
                return super.getInputStream();
            }
            switch (uri.getScheme().toLowerCase()) {
                case "file":
                    return Files.newInputStream(Paths.get(uri));
                case "http":
                case "https":
                    HttpURLConnection http = (HttpURLConnection) getURL().openConnection();
                    httpInterceptor.before(http);
                    int code = http.getResponseCode();
                    InputStream httpIn = code >= HttpURLConnection.HTTP_BAD_REQUEST
                            ? http.getErrorStream()
                            : http.getInputStream();
                    httpInterceptor.after(http);
                    return new FilterInputStream(httpIn) {
                        @Override
                        public void close() throws IOException {
                            super.close();
                            http.disconnect();
                        }
                    };
                case "ftp":
                    URLConnection ftp = getURL().openConnection();
                    ftpInterceptor.before(ftp);
                    InputStream ftpIn = ftp.getInputStream();
                    ftpInterceptor.after(ftp);
                    return ftpIn;
                default:
                    throw new UnsupportedOperationException("unsupported schema: " + uri.getScheme());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to open stream: " + getURL(), e);
        }
    }

    @Override
    public boolean exists() {
        if (uri.isAbsolute()) {
            if (uri.getScheme().equalsIgnoreCase("file")) {
                return Files.exists(Paths.get(uri));
            }
            throw new UnsupportedOperationException("unsupported scheme: " + uri.getScheme());
        }
        return super.exists();
    }

    @Override
    public URL getURL() {
        if (uri.isAbsolute()) {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getURL();
    }

    @Override
    public String getPath() {
        if (uri.isAbsolute()) {
            return uri.getPath();
        }
        return super.getPath();
    }

    @Override
    public String getFilenameExtension() {
        if (uri.isAbsolute()) {
            return getFileExtension(uri.getPath());
        }
        return super.getFilenameExtension();
    }

    @Override
    public String getFileName() {
        if (uri.isAbsolute()) {
            return getFileName(uri, true);
        }
        return super.getFileName();
    }

    /**
     * Configure the builtin http connection options by interceptor.
     * <p>
     * Do not call {@code connect()}, {@code disconnect()}, {@code getInputStream()} or {@code getOutputStream()}.
     *
     * @param interceptor http connection interceptor
     * @return this
     */
    public FileResource httpInterceptor(ConnectionInterceptor<HttpURLConnection> interceptor) {
        if (interceptor != null) {
            this.httpInterceptor = interceptor;
        }
        return this;
    }

    /**
     * Configure the builtin ftp connection options by interceptor.
     * <p>
     * Do not call {@code connect()}, {@code getInputStream()} or {@code getOutputStream()}.
     *
     * @param interceptor ftp connection interceptor
     * @return this
     */
    public FileResource ftpInterceptor(ConnectionInterceptor<URLConnection> interceptor) {
        if (interceptor != null) {
            this.ftpInterceptor = interceptor;
        }
        return this;
    }

    /**
     * Intercept the resource.
     *
     * @param path path
     */
    protected @Nullable Supplier<InputStream> resourceIntercept(final String path) {
        return null;
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
        return getFileName(URI.create(fullFileName), withExtension);
    }

    /**
     * Get file short name.
     *
     * @param uri           file uri
     * @param withExtension with extension or not
     * @return file short name
     */
    public static @NotNull String getFileName(URI uri, boolean withExtension) {
        String name = uri.getPath();
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

    /**
     * Invoked around the connection is opened.
     */
    public interface ConnectionInterceptor<T extends URLConnection> {
        /**
         * Invoke the connection after opened.
         *
         * @param connection connection
         */
        default void before(T connection) {
        }

        /**
         * Invoke the connection before closed.
         *
         * @param connection connection
         */
        default void after(T connection) {
        }
    }
}
