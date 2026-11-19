package com.example.bookmanagesystem.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
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
            throw e;
        }finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            int status = unhandledException ? 500 : response.getStatus();

            log.info(
                    "HTTP method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs
            );
            MDC.remove("requestId");
        }

    }

}
