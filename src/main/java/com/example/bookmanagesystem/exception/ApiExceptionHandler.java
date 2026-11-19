package com.example.bookmanagesystem.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String,String>errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(
                error->errors.putIfAbsent(error.getField(),error.getDefaultMessage())
        );
        Map<String, Object> body = errorBody("请求参数不合法");
        body.put("errors", errors);
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(
            DataAccessException exception, WebRequest request) {
        return handleExceptionInternal(exception, null, new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(
            Exception exception, WebRequest request) throws Exception {
        // 让后续的 Spring 解析器保留自定义 @ResponseStatus 异常的 HTTP 语义。
        if (AnnotatedElementUtils.hasAnnotation(exception.getClass(), ResponseStatus.class)) {
            throw exception;
        }
        return handleExceptionInternal(exception, null, new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String method = "unknown";
        String path = "unknown";
        if (request instanceof ServletWebRequest servletRequest) {
            method = servletRequest.getRequest().getMethod();
            path = servletRequest.getRequest().getRequestURI();
        }

        if (status.is5xxServerError()) {
            String message = exception instanceof DataAccessException
                    ? "数据库操作异常" : "请求处理发生异常";
            log.error("{} method={} path={} status={}",
                    message, method, path, status.value(), exception);
        } else {
            // 预期的 4xx 不打印请求体、令牌及可能包含提交内容的异常消息。
            log.warn("请求处理被拒绝 method={} path={} status={} reason={}",
                    method, path, status.value(), exception.getClass().getSimpleName());
        }

        Object responseBody = body instanceof Map<?, ?> ? body : errorBody(messageFor(status));
        return super.handleExceptionInternal(exception, responseBody, headers, status, request);
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        String requestId = MDC.get("requestId");
        if (requestId != null) {
            body.put("requestId", requestId);
        }
        return body;
    }

    private String messageFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "请求参数不合法";
            case 404 -> "请求的资源不存在";
            case 405 -> "请求方法不支持";
            case 415 -> "请求内容类型不支持";
            default -> status.is5xxServerError() ? "服务器处理失败"
                    : "请求处理失败（HTTP " + status.value() + "）";
        };
    }
}
