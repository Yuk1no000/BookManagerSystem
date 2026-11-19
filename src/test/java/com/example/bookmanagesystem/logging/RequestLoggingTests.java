package com.example.bookmanagesystem.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.bookmanagesystem.filter.RequestLogFilter;
import com.example.bookmanagesystem.service.BookService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "DEMO_READER_PASSWORD=test-only-password",
        "DEMO_LIBRARIAN_PASSWORD=test-only-password",
        "DEMO_ADMIN_PASSWORD=test-only-password"
})
@AutoConfigureMockMvc
class RequestLoggingTests {
    private static final String PASSWORD = "test-only-password";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookService bookService;

    private Logger root;
    private ListAppender<ILoggingEvent> logs;

    @BeforeEach
    void captureLogs() {
        root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logs = new ListAppender<>() {
            @Override
            protected void append(ILoggingEvent event) {
                event.prepareForDeferredProcessing();
                super.append(event);
            }
        };
        logs.setContext(root.getLoggerContext());
        logs.start();
        root.addAppender(logs);
    }

    @AfterEach
    void releaseLogs() {
        root.detachAppender(logs);
        logs.stop();
        MDC.remove("requestId");
    }

    @Test
    void successfulRequestsHaveUniqueIdsAndLeaveNoMdcBehind() throws Exception {
        MvcResult first = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        String firstId = assertRequestLogged(first, 200);
        MvcResult second = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andReturn();
        assertNotEquals(firstId, assertRequestLogged(second, 200));
    }

    @Test
    void unauthenticatedRequestIsLoggedBeforeSecurityReturns401() throws Exception {
        MvcResult result = mvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized()).andReturn();
        String id = assertRequestLogged(result, 401);
        assertTrue(eventsFor(id).stream().anyMatch(event ->
                event.getLevel() == Level.WARN && event.getFormattedMessage().contains("未登录访问")));
    }

    @Test
    void failedLoginIsCorrelatedWithoutLoggingSubmittedPassword() throws Exception {
        CsrfSession csrf = csrf(null);
        logs.list.clear();
        String wrongPassword = "never-log-this-submitted-password";
        MvcResult result = mvc.perform(post("/api/auth/login").session(csrf.session())
                        .header(csrf.header(), csrf.token())
                        .param("username", "admin").param("password", wrongPassword))
                .andExpect(status().isUnauthorized()).andReturn();
        String id = assertRequestLogged(result, 401);
        assertTrue(eventsFor(id).stream().anyMatch(event -> event.getFormattedMessage().contains("登录失败")));
        assertTrue(logs.list.stream().noneMatch(event -> event.getFormattedMessage().contains(wrongPassword)));
    }

    @Test
    void loginAndLogoutBothHaveRequestIdsAndLifecycleLogs() throws Exception {
        MockHttpSession session = login("admin");
        assertTrue(logs.list.stream().anyMatch(event -> event.getFormattedMessage().contains("登录成功 user=admin")));
        CsrfSession csrf = csrf(session);
        logs.list.clear();
        MvcResult result = mvc.perform(post("/api/auth/logout").session(csrf.session())
                        .header(csrf.header(), csrf.token()))
                .andExpect(status().isNoContent()).andReturn();
        String id = assertRequestLogged(result, 204);
        assertTrue(eventsFor(id).stream().anyMatch(event -> event.getFormattedMessage().contains("退出登录")));
    }

    @Test
    void readerCannotDeleteAndThe403HasCorrelatedWarning() throws Exception {
        CsrfSession csrf = csrf(login("reader"));
        logs.list.clear();
        MvcResult result = mvc.perform(delete("/api/books/99").session(csrf.session())
                        .header(csrf.header(), csrf.token()))
                .andExpect(status().isForbidden()).andReturn();
        String id = assertRequestLogged(result, 403);
        assertTrue(eventsFor(id).stream().anyMatch(event -> event.getFormattedMessage().contains("访问被拒绝")));
    }

    @Test
    void missingCsrfTokenIsLoggedEvenWhenSecurityRejectsRequest() throws Exception {
        MockHttpSession session = login("admin");
        logs.list.clear();
        MvcResult result = mvc.perform(delete("/api/books/99").session(session))
                .andExpect(status().isForbidden()).andReturn();
        String id = assertRequestLogged(result, 403);
        assertTrue(eventsFor(id).stream().anyMatch(event -> event.getFormattedMessage().contains("Csrf")));
    }

    @Test
    void invalidJsonRetains400AndDoesNotLogSubmittedBody() throws Exception {
        CsrfSession csrf = csrf(login("admin"));
        logs.list.clear();
        String submittedValue = "do-not-log-request-body";
        MvcResult result = mvc.perform(post("/api/books").session(csrf.session())
                        .header(csrf.header(), csrf.token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + submittedValue + "\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请求参数不合法")).andReturn();
        String id = assertRequestLogged(result, 400);
        assertTrue(result.getResponse().getContentAsString(StandardCharsets.UTF_8).contains(id));
        assertTrue(eventsFor(id).stream().noneMatch(event -> event.getLevel() == Level.ERROR
                || event.getFormattedMessage().contains(submittedValue)));
    }

    @Test
    void fieldValidationStillReturnsFieldErrors() throws Exception {
        CsrfSession csrf = csrf(login("admin"));
        MvcResult result = mvc.perform(post("/api/books").session(csrf.session())
                        .header(csrf.header(), csrf.token()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists()).andReturn();
        assertRequestLogged(result, 400);
    }

    @Test
    void missingBookRetains404() throws Exception {
        MvcResult result = mvc.perform(get("/api/books/99").session(login("reader")))
                .andExpect(status().isNotFound()).andReturn();
        assertRequestLogged(result, 404);
    }

    @Test
    void databaseFailureProducesOneCorrelatedStackTraceAndSafeResponse() throws Exception {
        assertServerFailure(new DataAccessResourceFailureException("diagnostic-database-offline"),
                "数据库操作异常");
    }

    @Test
    void unexpectedFailureIsWrittenToFileWithStackTrace() throws Exception {
        String id = assertServerFailure(new IllegalStateException("diagnostic-unexpected-failure"),
                "请求处理发生异常");
        String file = Files.readString(Path.of("target/logging-tests/book-manage.log"), StandardCharsets.UTF_8);
        assertTrue(file.contains("requestId=" + id));
        assertTrue(file.contains("java.lang.IllegalStateException: diagnostic-unexpected-failure"));
    }

    private String assertServerFailure(RuntimeException failure, String expectedMessage) throws Exception {
        MockHttpSession session = login("admin");
        when(bookService.getAllBooks()).thenThrow(failure);
        logs.list.clear();
        MvcResult result = mvc.perform(get("/api/books").session(session))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("服务器处理失败")).andReturn();
        String id = assertRequestLogged(result, 500);
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(body.contains(id));
        assertFalse(body.contains(failure.getMessage()));
        List<ILoggingEvent> errors = eventsFor(id).stream()
                .filter(event -> event.getLevel() == Level.ERROR).toList();
        assertEquals(1, errors.size(), "MVC 异常应只记录一次堆栈");
        assertTrue(errors.get(0).getFormattedMessage().contains(expectedMessage));
        assertNotNull(errors.get(0).getThrowableProxy());
        return id;
    }

    private MockHttpSession login(String username) throws Exception {
        CsrfSession csrf = csrf(null);
        MvcResult result = mvc.perform(post("/api/auth/login").session(csrf.session())
                        .header(csrf.header(), csrf.token())
                        .param("username", username).param("password", PASSWORD))
                .andExpect(status().isNoContent()).andReturn();
        assertRequestLogged(result, 204);
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    private CsrfSession csrf(MockHttpSession session) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/auth/csrf");
        if (session != null) request.session(session);
        MvcResult result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        CsrfToken token = (CsrfToken) result.getRequest().getAttribute(CsrfToken.class.getName());
        assertNotNull(token);
        return new CsrfSession((MockHttpSession) result.getRequest().getSession(false),
                token.getHeaderName(), token.getToken());
    }

    private String assertRequestLogged(MvcResult result, int status) {
        String id = result.getResponse().getHeader("X-Request-ID");
        assertNotNull(id);
        assertDoesNotThrow(() -> UUID.fromString(id));
        assertNull(MDC.get("requestId"), "请求结束后必须清理 MDC");
        assertTrue(eventsFor(id).stream().anyMatch(event ->
                event.getLoggerName().equals(RequestLogFilter.class.getName())
                        && event.getFormattedMessage().startsWith("HTTP method=")
                        && event.getFormattedMessage().contains("status=" + status)));
        return id;
    }

    private List<ILoggingEvent> eventsFor(String id) {
        return logs.list.stream().filter(event -> id.equals(event.getMDCPropertyMap().get("requestId"))).toList();
    }

    private record CsrfSession(MockHttpSession session, String header, String token) {}
}
