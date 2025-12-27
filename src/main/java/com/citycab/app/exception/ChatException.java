package com.citycab.app.exception;

public class ChatException extends RuntimeException {
    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatException(String message) {
        super(message);
    }
}