package com.github.chengyuxing.common.io;

import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Classpath resource.
 */
public class ClassPathResource {
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final String path;
    private final ClassLoader classLoader;
    private final int bufferSize;

    /**
     * Constructs a new ClassPathResource with file path.
     *
     * @param path file path
     */
    public ClassPathResource(@NotNull @Pattern("[^/].*") String path) {
        this.path = cleanPath(path);
        this.classLoader = getClassLoader();
        this.bufferSize = DEFAULT_BUFFER_SIZE;
    }

    /**
     * Constructs a new ClassPathResource with file path.
     *
     * @param path       file path
     * @param bufferSize buffer size
     */
    public ClassPathResource(@NotNull @Pattern("[^/].*") String path, int bufferSize) {
        this.path = cleanPath(path);
        this.classLoader = getClassLoader();
        this.bufferSize = bufferSize;
    }

    /**
     * If path starts with '{@code /}', remove it.
     *
     * @param path path
     * @return correct classpath resource path
     */
    private String cleanPath(@NotNull String path) {
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
        return new BufferedReader(new InputStreamReader(getInputStream(), charset), bufferSize);
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
        return readBytes(getInputStream(), bufferSize);
    }

    /**
     * Current resource transfer to another.
     *
     * @param out output stream
     * @throws IOException if file not exists
     */
    public void transferTo(OutputStream out) throws IOException {
        transferTo(getInputStream(), out, 0, Long.MAX_VALUE, bufferSize);
    }

    /**
     * Current resource part transfer to another.
     *
     * @param out    output stream
     * @param offset resource part bytes offset position
     * @param length resource part bytes length
     * @throws IOException if file not exists
     */
    public void transferTo(OutputStream out, long offset, long length) throws IOException {
        transferTo(getInputStream(), out, offset, length, bufferSize);
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
        if (isFileURL(url)) {
            File f = Paths.get(url.toURI()).toFile();
            dt = f.lastModified();
        } else if (isJarURL(url)) {
            dt = -1;
        }
        return dt;
    }

    /**
     * Read all bytes.
     *
     * @param in input stream
     * @return all bytes
     * @throws IOException if file not exists
     */
    public static byte[] readBytes(InputStream in) throws IOException {
        return readBytes(in, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Read all bytes.
     *
     * @param in         input stream
     * @param bufferSize buffer size, in bytes
     * @return all bytes
     * @throws IOException if file not exists
     */
    public static byte[] readBytes(InputStream in, int bufferSize) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            transferTo(in, out, 0, Long.MAX_VALUE, bufferSize);
            return out.toByteArray();
        }
    }

    /**
     * Transfer input to output.
     *
     * @param in     input stream
     * @param out    output stream
     * @param offset bytes offset position
     * @param length bytes length
     * @throws IOException if file not exists
     */
    public static void transferTo(InputStream in, OutputStream out, long offset, long length) throws IOException {
        transferTo(in, out, offset, length, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Transfer input to output.
     *
     * @param in         input stream
     * @param out        output stream
     * @param offset     bytes offset position
     * @param length     bytes length
     * @param bufferSize buffer size, in bytes
     * @throws IOException if file not exists
     */
    public static void transferTo(InputStream in, OutputStream out, long offset, long length, int bufferSize) throws IOException {
        try (ReadableByteChannel inChannel = Channels.newChannel(in);
             WritableByteChannel outChannel = Channels.newChannel(out)) {
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

            if (offset > 0) {
                long skipped = 0L;
                // if the resource is http(s), ftp or any other resource from remote
                // discard buffer may be more precision than in.skip()
                byte[] skipBuffer = new byte[DEFAULT_BUFFER_SIZE];
                while (skipped < offset) {
                    long toSkip = Math.min(skipBuffer.length, offset - skipped);
                    int n = in.read(skipBuffer, 0, (int) toSkip);
                    if (n == -1) break;
                    skipped += n;
                }
            }
            long totalRead = 0L;
            while (totalRead < length) {
                int toRead = (int) Math.min(buffer.capacity(), length - totalRead);
                buffer.clear();
                buffer.limit(toRead);

                int read = inChannel.read(buffer);
                if (read == -1) {
                    break;
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    outChannel.write(buffer);
                }
                totalRead += read;
            }
        }
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

    /**
     * Check is file url or not by protocol.
     *
     * @param url url
     * @return true or false
     */
    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        return "file".equals(protocol) || "vfsfile".equals(protocol) || "vfs".equals(protocol);
    }

    /**
     * Check is jar url or not by protocol.
     *
     * @param url url
     * @return true or false
     */
    public static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        return "jar".equals(protocol) || "war".equals(protocol) || "zip".equals(protocol) || "vfszip".equals(protocol) || "wsjar".equals(protocol);
    }
}
