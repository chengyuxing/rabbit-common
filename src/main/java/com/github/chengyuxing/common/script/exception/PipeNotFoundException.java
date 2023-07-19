package com.github.chengyuxing.common.script.exception;

public class PipeNotFoundException extends RuntimeException {
    public PipeNotFoundException(String message) {
        super(message);
    }

    public PipeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
