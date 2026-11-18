package com.example.bookmanagesystem.exception;


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
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> handleValidation(MethodArgumentNotValidException exception){
        Map<String,String>errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(
                error->errors.putIfAbsent(error.getField(),error.getDefaultMessage())
        );
        return Map.of(
                "message","请求参数",
                "errors",errors
        );
    }
}
