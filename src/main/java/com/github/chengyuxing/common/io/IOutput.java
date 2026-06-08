package com.github.chengyuxing.common.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple output operation interface.
 */
@FunctionalInterface
public interface IOutput {

    void writeTo(OutputStream out) throws IOException;

    default void writeTo(OutputStream out, boolean flush) throws IOException {
        writeTo(out);
        if (flush) {
            out.flush();
        }
    }

    default void writeTo(Path path) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            writeTo(out);
        }
    }

    default void writeTo(String path) throws IOException {
        writeTo(Paths.get(path));
    }

    default void writeTo(File file) throws IOException {
        writeTo(file.toPath());
    }

    default byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeTo(out);
        return out.toByteArray();
    }
}
