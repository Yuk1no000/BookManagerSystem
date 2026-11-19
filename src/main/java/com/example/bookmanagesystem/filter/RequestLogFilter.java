package com.example.bookmanagesystem.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Order(SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1)
public class RequestLogFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException{
        String requestId = UUID.randomUUID().toString();
        long start = System.nanoTime();
        boolean unhandledException = false;

        MDC.put("requestId", requestId);
        response.setHeader("X-Request-ID", requestId);

        try{
            filterChain.doFilter(request, response);
        }catch(IOException | ServletException | RuntimeException e){
            unhandledException = true;
            // MVC 中已处理的异常不会到达这里；记录过滤器链中继续向外抛出的异常。
            log.error("请求处理发生未捕获异常 method={} path={}",
                    request.getMethod(), request.getRequestURI(), e);
            throw e;
        }finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            int status = unhandledException ? 500 : response.getStatus();

            try {
                log.info(
                        "HTTP method={} path={} status={} durationMs={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        status,
                        durationMs
                );
            } finally {
                MDC.remove("requestId");
            }
        }
    }
}
