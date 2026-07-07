package com.panel.engine;

// 入参/业务态校验失败(如人数越界)
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
