package com.citycab.app.common;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private String message;
    private int statusCode;
    private T data;
    private LocalDateTime time = LocalDateTime.now();

    // Success complet
    public static <T> BaseResponse<T> success(String message, T data, int statusCode) {
        return new BaseResponse<>(message, statusCode, data, LocalDateTime.now());
    }

    // Success avec data et message par défaut
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>("Success", 200, data, LocalDateTime.now());
    }

    // Success sans data
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>("Success Operation", 200, null, LocalDateTime.now());
    }

    // Error complet
    public static <T> BaseResponse<T> error(String message, int statusCode) {
        return new BaseResponse<>(message, statusCode, null, LocalDateTime.now());
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(message, 500, null, LocalDateTime.now());
    }

    // Error avec message par défaut
    public static <T> BaseResponse<T> error(int statusCode) {
        return new BaseResponse<>("An error occurred", statusCode, null, LocalDateTime.now());
    }

    // Error avec tout par défaut
    public static <T> BaseResponse<T> error() {
        return new BaseResponse<>("An error occurred", 500, null, LocalDateTime.now());
    }
}
