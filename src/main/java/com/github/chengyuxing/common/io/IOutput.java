package com.github.chengyuxing.common.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple output operation interface.
 */
public interface IOutput {
    /**
     * To bytes.
     *
     * @return bytes
     * @throws IOException if io state error
     */
    byte[] toBytes() throws IOException;

    default void saveTo(OutputStream outputStream, boolean close) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        out.write(toBytes());
        if (close) {
            out.flush();
            out.close();
        }
    }

    default void saveTo(OutputStream outputStream) throws IOException {
        saveTo(outputStream, true);
    }

    default void saveTo(String path) throws IOException {
        saveTo(Files.newOutputStream(Paths.get(path)));
    }

    default void saveTo(File file) throws IOException {
        saveTo(Files.newOutputStream(file.toPath()));
    }

    default void saveTo(Path path) throws IOException {
        saveTo(Files.newOutputStream(path));
    }
}
