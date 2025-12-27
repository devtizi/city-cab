package com.citycab.app.exception;

//@EqualsAndHashCode(callSuper = true)
public class NotAuthorizeException extends RuntimeException{

    public NotAuthorizeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAuthorizeException(String message) {
        super(message);
    }
}
