package com.github.chengyuxing.common.script.exception;

public class ScriptSyntaxException extends RuntimeException {
    public ScriptSyntaxException(String message) {
        super(message);
    }

    public ScriptSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptSyntaxException(Throwable cause) {
        super(cause);
    }
}
