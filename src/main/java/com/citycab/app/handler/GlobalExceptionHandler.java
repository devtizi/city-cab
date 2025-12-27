package com.citycab.app.handler;

import java.nio.file.AccessDeniedException;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import com.citycab.app.common.BaseResponse;
import com.citycab.app.exception.NotAuthorizeException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleException(MethodArgumentNotValidException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<BaseResponse<?>> handleException(ClientAbortException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<?>> handleException(RuntimeException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ResourceAccessException.class})
    public ResponseEntity<BaseResponse<?>> handleException(ResourceAccessException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.REQUEST_TIMEOUT.value());
        return new ResponseEntity<>(response, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseResponse<?>> handleException(BadCredentialsException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error("Username and / or password is incorrect", HttpStatus.REQUEST_TIMEOUT.value());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleException(NotFoundException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<BaseResponse<?>> handleException(SecurityException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<BaseResponse<?>> handleException(MessagingException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleException(UsernameNotFoundException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotAuthorizeException.class)
    public ResponseEntity<BaseResponse<?>> handleException(NotAuthorizeException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleException(Exception exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<?>> handleException(AccessDeniedException exp) {
        log.error(exp.getMessage(), exp);
        BaseResponse<Object> response = BaseResponse.error(exp.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}