package com.panel.web;

import com.panel.ai.AiUpstreamException;
import com.panel.engine.ValidationException;
import com.panel.web.dto.ErrorBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 统一错误体 {code,message}
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorBody validation(ValidationException e) {
        return new ErrorBody("VALIDATION_ERROR", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorBody notFound(NotFoundException e) {
        return new ErrorBody("NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(InvalidStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorBody invalidState(InvalidStateException e) {
        return new ErrorBody("INVALID_STATE", e.getMessage());
    }

    @ExceptionHandler(AiUpstreamException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorBody upstream(AiUpstreamException e) {
        return new ErrorBody("AI_UPSTREAM_ERROR", e.getMessage());
    }
}
