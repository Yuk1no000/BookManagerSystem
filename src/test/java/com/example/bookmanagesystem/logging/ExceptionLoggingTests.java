package com.example.bookmanagesystem.logging;

import com.example.bookmanagesystem.controller.BookController;
import com.example.bookmanagesystem.exception.ApiExceptionHandler;
import com.example.bookmanagesystem.filter.RequestLogFilter;
import com.example.bookmanagesystem.service.BookService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExceptionLoggingTests {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new BookController(mock(BookService.class)), new StatusController())
                .setControllerAdvice(new ApiExceptionHandler())
                .addFilters(new RequestLogFilter()).build();
    }

    @Test
    void unsupportedMethodRetains405AndAllowHeader() throws Exception {
        mvc.perform(patch("/api/books"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.message").value("请求方法不支持"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void invalidIdRetains400() throws Exception {
        MvcResult result = mvc.perform(get("/api/books/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.requestId").exists()).andReturn();
        assertNotNull(result.getResponse().getHeader("X-Request-ID"));
    }

    @Test
    void annotatedExceptionRetainsItsOriginalStatus() throws Exception {
        mvc.perform(get("/api/test/annotated-not-found"))
                .andExpect(status().isNotFound());
        assertNull(MDC.get("requestId"));
    }

    @Test
    void exceptionOutsideMvcStillHasRequestIdAndCleansMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/filter-failure");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThrows(ServletException.class, () -> new RequestLogFilter().doFilter(request, response, (req, res) -> {
            assertNotNull(MDC.get("requestId"));
            throw new ServletException("diagnostic-filter-failure");
        }));
        assertNotNull(response.getHeader("X-Request-ID"));
        assertNull(MDC.get("requestId"));
    }

    @RestController
    static class StatusController {
        @GetMapping("/api/test/annotated-not-found")
        void missingResource() {
            throw new MissingResourceException();
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class MissingResourceException extends RuntimeException {}
}
