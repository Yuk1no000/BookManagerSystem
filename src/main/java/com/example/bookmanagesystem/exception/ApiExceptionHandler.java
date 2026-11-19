package com.example.bookmanagesystem.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> handleValidation(MethodArgumentNotValidException exception){
        Map<String,String>errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(
                error->errors.putIfAbsent(error.getField(),error.getDefaultMessage())
        );
        return Map.of(
                "message","服务器处理失败"
        );
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String,Object> handleDataAccessException(DataAccessException exception) {
        log.error("数据库操作异常", exception);
        return Map.of(
                "message", "数据库操作异常",
                "error", exception.getMessage()
        );
    }
}
