package com.github.chengyuxing.common.io;

import com.github.chengyuxing.common.DataRow;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
    //language=regexp
    private final static String SCHEMAS_PATTERN = "(file|http|https|ftp)://.+";
    private final DataRow properties = new DataRow();

    /**
     * Constructs a new FileResource with file path.
     *
     * @param path file path
     * @see FileResource
     */
    public FileResource(String path) {
        super(path);
    }

    /**
     * Constructs a new FileResource with file path.
     *
     * @param path       file path
     * @param properties properties e.g. http(s) request property {@code headers}:{@link Map}
     * @see FileResource
     */
    public FileResource(String path, Map<String, Object> properties) {
        super(path);
        if (Objects.nonNull(properties)) {
            this.properties.putAll(properties);
        }
    }

    /**
     * Check resource is URI or not.
     *
     * @return true if is URI or false
     */
    private boolean isURI() {
        return path.matches(SCHEMAS_PATTERN) || path.startsWith("file:");
    }

    @Override
    public InputStream getInputStream() {
        if (isURI()) {
            try {
                String schema = uriSchema(path);
                switch (schema) {
                    case "file":
                        //noinspection IOStreamConstructor
                        return new FileInputStream(getURL().getFile());
                    case "http":
                    case "https":
                        HttpURLConnection httpCon = (HttpURLConnection) getURL().openConnection();
                        httpCon.setRequestMethod("GET");
                        if (properties.containsKey("connectTimeout")) {
                            httpCon.setConnectTimeout(properties.getInt("connectTimeout", 5000));
                        }
                        if (properties.containsKey("readTimeout")) {
                            httpCon.setReadTimeout(properties.getInt("readTimeout", 5000));
                        }
                        if (properties.containsKey("headers")) {
                            Map<String, Object> headers = properties.getAs("headers", new LinkedHashMap<>());
                            for (Map.Entry<String, Object> e : headers.entrySet()) {
                                httpCon.setRequestProperty(e.getKey(), e.getValue().toString());
                            }
                        }
                        return httpCon.getInputStream();
                    case "ftp":
                        URLConnection ftpCon = getURL().openConnection();
                        if (properties.containsKey("connectTimeout")) {
                            ftpCon.setConnectTimeout(properties.getInt("connectTimeout", 5000));
                        }
                        if (properties.containsKey("readTimeout")) {
                            ftpCon.setReadTimeout(properties.getInt("readTimeout", 5000));
                        }
                        return ftpCon.getInputStream();
                    default:
                        throw new UnsupportedOperationException("unknown schema");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return super.getInputStream();
    }

    @Override
    public URL getURL() {
        if (isURI()) {
            try {
                String schema = uriSchema(path);
                switch (schema) {
                    case "file":
                        URI uri = URI.create(path);
                        Path p = Paths.get(uri);
                        if (!Files.isDirectory(p) && Files.exists(p)) {
                            return uri.toURL();
                        }
                        return null;
                    case "http":
                    case "https":
                    case "ftp":
                        return new URL(path);
                    default:
                        throw new UnsupportedOperationException("unknown schema");
                }
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

    @Override
    public String getFilenameExtension() {
        if (isURI()) {
            String schema = uriSchema(path);
            if (schema.startsWith("http")) {
                String fullFileName = cleanHttpUri(path);
                return getFileExtension(fullFileName);
            }
        }
        return super.getFilenameExtension();
    }

    @Override
    public String getFileName() {
        if (isURI()) {
            String schema = uriSchema(path);
            if (schema.startsWith("http")) {
                return getFileName(path, true);
            }
        }
        return super.getFileName();
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
        if (fullFileName.matches(SCHEMAS_PATTERN) || fullFileName.startsWith("file:")) {
            String schema = uriSchema(fullFileName);
            switch (schema) {
                case "file":
                    name = Paths.get(URI.create(fullFileName)).getFileName().toString();
                    break;
                case "http":
                case "https":
                case "ftp":
                    fullFileName = cleanHttpUri(fullFileName);
                    int pIdx = fullFileName.lastIndexOf('/');
                    name = fullFileName.substring(pIdx + 1);
                    break;
                default:
                    throw new UnsupportedOperationException("unknown schema");
            }
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
    public static String formatFileSize(byte[] bytes) {
        return formatFileSize(bytes.length);
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
     * Remove # and ? parts.
     *
     * @param uri http uri
     * @return http uri
     */
    private static String cleanHttpUri(String uri) {
        String fullFileName = uri;
        int hashIdx = fullFileName.indexOf('#');
        if (hashIdx != -1) {
            fullFileName = fullFileName.substring(0, hashIdx);
        }
        int qIdx = fullFileName.indexOf('?');
        if (qIdx != -1) {
            fullFileName = fullFileName.substring(0, qIdx);
        }
        return fullFileName;
    }

    /**
     * Get uri schema.
     *
     * @param uri uri
     * @return schema
     */
    private static String uriSchema(String uri) {
        return uri.substring(0, uri.indexOf(':'));
    }
}
