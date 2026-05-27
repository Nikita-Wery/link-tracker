package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ScrapperServerException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.UnknownScrapperApiException;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.ObjectMapper;

@Tag("integration")
@SpringBootTest(
        properties = {
            "spring.autoconfigure.exclude=" + "org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration",
            "spring.jpa.hibernate.ddl-auto=none",
            "app.telegram.enabled=false",
            "app.client.scrapper.api.kafka.enabled=false",
            "app.client.scrapper.host=${wiremock.server.baseUrl}",
            "app.client.scrapper.api.grpc.enabled=false",
        })
@ActiveProfiles("circuit-breaker-test")
@EnableWireMock
@DisplayName("Circuit Breaker tests for ScrapperClient")
class ScrapperApiCircuitBreakerTest {

    private static final long TEST_CHAT_ID = 1L;
    private static final String TEST_LINK = "https://github.com";
    private static final Set<String> TEST_TAGS = Set.of("test", "github");
    private static final Long TEST_LINK_ID = 1L;

    @Autowired
    private CircuitBreakerRegistry registry;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScrapperClient scrapperClient;

    private CircuitBreaker circuitBreaker;
    private CircuitBreakerConfig config;

    @BeforeEach
    void setUp() {
        circuitBreaker = registry.circuitBreaker("scrapperApi");
        config = circuitBreaker.getCircuitBreakerConfig();
    }

    @AfterEach
    void resetCircuitBreaker() {
        circuitBreaker.reset();
        WireMock.reset();
    }

    @Test
    @DisplayName("Should not process request when circuit breaker is OPEN")
    void shouldNotProcessRequest_whenOpenState() {
        int minNum = config.getMinimumNumberOfCalls();

        String path = "/links";
        String method = "GET";

        setupErrorStub(path, method, HttpStatus.INTERNAL_SERVER_ERROR.value());

        openCircuitBreakerForEndpoint(path, method, minNum);

        long start = System.currentTimeMillis();
        assertThrows(CallNotPermittedException.class, () -> scrapperClient.getLinks(TEST_CHAT_ID));
        long duration = System.currentTimeMillis() - start;

        assertTrue(duration < 100, "Request should be rejected immediately");
        verify(minNum, getRequestedFor(urlEqualTo(path)));
    }

    @ParameterizedTest
    @MethodSource("allEndpointsProvider")
    @DisplayName("Should transition from HALF_OPEN to CLOSED when successful")
    void shouldChangeToClosedStateFromHalfOpen_whenSuccessfulResults(EndpointInfo endpoint) {

        int minNum = config.getMinimumNumberOfCalls();
        setupErrorStub(endpoint.path(), endpoint.method(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        openCircuitBreakerForEndpoint(endpoint.path(), endpoint.method(), minNum);

        waitForHalfOpenState();

        int permittedNumInHalfOpen = config.getPermittedNumberOfCallsInHalfOpenState();
        setupSuccessStub(endpoint.path(), endpoint.method(), endpoint.successResponseBody());

        for (int i = 0; i < permittedNumInHalfOpen; i++) {
            assertDoesNotThrow(() -> endpoint.invoke(scrapperClient, TEST_CHAT_ID));
        }

        assertEquals(
                CLOSED,
                circuitBreaker.getState(),
                "Circuit breaker should transition to CLOSED after successful calls");
    }

    @ParameterizedTest
    @MethodSource("allEndpointsProvider")
    @DisplayName("Should remain OPEN when failing in HALF_OPEN state")
    void shouldChangeToOpenStateFromHalfOpen_whenErrorResult(EndpointInfo endpoint) {

        int minNum = config.getMinimumNumberOfCalls();
        setupErrorStub(endpoint.path(), endpoint.method(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        openCircuitBreakerForEndpoint(endpoint.path(), endpoint.method(), minNum);

        waitForHalfOpenState();

        int permittedNumInHalfOpen = config.getPermittedNumberOfCallsInHalfOpenState();
        setupErrorStub(endpoint.path(), endpoint.method(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        for (int i = 0; i < permittedNumInHalfOpen; i++) {
            assertThrows(ScrapperServerException.class, () -> endpoint.invoke(scrapperClient, TEST_CHAT_ID));
        }

        assertEquals(
                OPEN,
                circuitBreaker.getState(),
                "Circuit breaker should return to OPEN after failed calls in HALF_OPEN");
    }

    @Test
    @DisplayName("Should handle 4xx errors without affecting circuit breaker")
    void shouldHandle4xxErrorsWithoutOpeningCircuitBreaker() {

        String errorResponse = getErrorJsonString("UnknownScrapperApiException");
        stubFor(get(urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo(String.valueOf(TEST_CHAT_ID)))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponse)));

        for (int i = 0; i < config.getMinimumNumberOfCalls(); i++) {
            assertThrows(UnknownScrapperApiException.class, () -> scrapperClient.getLinks(TEST_CHAT_ID));
        }

        assertEquals(CLOSED, circuitBreaker.getState(), "Circuit breaker should remain CLOSED after 4xx errors");
    }

    private void openCircuitBreakerForEndpoint(String path, String method, int minNum) {
        for (int i = 0; i < minNum; i++) {
            assertThrows(ScrapperServerException.class, () -> invokeEndpointByPath(path, method, TEST_CHAT_ID));
        }
        assertEquals(OPEN, circuitBreaker.getState(), "Circuit breaker should be OPEN after " + minNum + " failures");

        switch (method) {
            case "GET":
                verify(minNum, getRequestedFor(urlPathEqualTo(path)));
                break;
            case "POST":
                verify(minNum, postRequestedFor(urlPathEqualTo(path)));
                break;
            case "DELETE":
                verify(minNum, deleteRequestedFor(urlPathEqualTo(path)));
                break;
            default:
                verify(minNum, anyRequestedFor(urlPathEqualTo(path)));
        }
    }

    private void waitForHalfOpenState() {
        long waitInOpenMs = config.getWaitIntervalFunctionInOpenState().apply(1);
        await().atMost(Duration.ofMillis(waitInOpenMs + 1000))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> circuitBreaker.getState() == HALF_OPEN);
    }

    private void invokeEndpointByPath(String path, String method, Long chatId) {
        if (path.equals("/links") && method.equals("GET")) {
            scrapperClient.getLinks(chatId);
        } else if (path.equals("/links") && method.equals("POST")) {
            scrapperClient.addLink(chatId, new AddLinkRequest(TEST_LINK, TEST_TAGS));
        } else if (path.equals("/links") && method.equals("DELETE")) {
            scrapperClient.untrackLink(chatId, new RemoveLinkRequest(TEST_LINK));
        } else if (path.equals("/tg-chat/" + TEST_CHAT_ID) && method.equals("POST")) {
            scrapperClient.registerChat(TEST_CHAT_ID);
        } else if (path.equals("/tg-chat/" + TEST_CHAT_ID) && method.equals("DELETE")) {
            scrapperClient.deleteChat(TEST_CHAT_ID);
        }
    }

    private void setupErrorStub(String path, String method, int statusCode) {
        String errorResponse = getErrorJsonString("ScrapperServerException");

        switch (method) {
            case "GET":
                stubFor(get(urlPathEqualTo(path))
                        .willReturn(aResponse()
                                .withStatus(statusCode)
                                .withHeader("Content-Type", "application/json")
                                .withBody(errorResponse)));
                break;
            case "POST":
                stubFor(post(urlPathEqualTo(path))
                        .willReturn(aResponse()
                                .withStatus(statusCode)
                                .withHeader("Content-Type", "application/json")
                                .withBody(errorResponse)));
                break;
            case "DELETE":
                stubFor(delete(urlPathEqualTo(path))
                        .willReturn(aResponse()
                                .withStatus(statusCode)
                                .withHeader("Content-Type", "application/json")
                                .withBody(errorResponse)));
                break;
            default:
                stubFor(any(urlPathEqualTo(path))
                        .willReturn(aResponse()
                                .withStatus(statusCode)
                                .withHeader("Content-Type", "application/json")
                                .withBody(errorResponse)));
        }
    }

    private void setupSuccessStub(String path, String method, String responseBody) {
        switch (method) {
            case "GET":
                stubFor(get(urlPathEqualTo(path))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody)));
                break;
            case "POST":
                stubFor(post(urlPathEqualTo(path))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody)));
                break;
            case "DELETE":
                stubFor(delete(urlPathEqualTo(path))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody)));
                break;
            default:
                stubFor(any(urlPathEqualTo(path))
                        .willReturn(aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody)));
        }
    }

    private String getErrorJsonString(String exceptionName) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .description("test")
                .code("test")
                .exceptionName(exceptionName)
                .exceptionMessage("test")
                .stackTrace(List.of())
                .build();
        return writeValueAsString(error);
    }

    private String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<EndpointInfo> allEndpointsProvider() {
        return Stream.of(
                new EndpointInfo(
                        "GET /links",
                        "/links",
                        "GET",
                        (client, chatId) -> client.getLinks(chatId),
                        () -> new ListLinksResponse(List.of(), 0)),
                new EndpointInfo(
                        "POST /links",
                        "/links",
                        "POST",
                        (client, chatId) -> client.addLink(chatId, new AddLinkRequest(TEST_LINK, TEST_TAGS)),
                        () -> new LinkResponse(
                                TEST_LINK_ID,
                                URI.create(TEST_LINK),
                                TEST_TAGS.stream().toList())),
                new EndpointInfo(
                        "DELETE /links",
                        "/links",
                        "DELETE",
                        (client, chatId) -> client.untrackLink(chatId, new RemoveLinkRequest(TEST_LINK)),
                        () -> new LinkResponse(
                                TEST_LINK_ID,
                                URI.create(TEST_LINK),
                                TEST_TAGS.stream().toList())),
                new EndpointInfo(
                        "POST /tg-chat/{id}",
                        "/tg-chat/" + TEST_CHAT_ID,
                        "POST",
                        (client, chatId) -> client.registerChat(chatId),
                        () -> null),
                new EndpointInfo(
                        "DELETE /tg-chat/{id}",
                        "/tg-chat/" + TEST_CHAT_ID,
                        "DELETE",
                        (client, chatId) -> client.deleteChat(chatId),
                        () -> null));
    }

    static class EndpointInfo {
        private final String name;
        private final String path;
        private final String method;
        private final EndpointInvoker invoker;
        private final ResponseBodyProvider responseProvider;

        EndpointInfo(
                String name,
                String path,
                String method,
                EndpointInvoker invoker,
                ResponseBodyProvider responseProvider) {
            this.name = name;
            this.path = path;
            this.method = method;
            this.invoker = invoker;
            this.responseProvider = responseProvider;
        }

        String name() {
            return name;
        }

        String path() {
            return path;
        }

        String method() {
            return method;
        }

        void invoke(ScrapperClient client, Long chatId) {
            invoker.invoke(client, chatId);
        }

        String successResponseBody() {
            Object body = responseProvider.getBody();
            if (body == null) return "";
            try {
                return new ObjectMapper().writeValueAsString(body);
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @FunctionalInterface
    interface EndpointInvoker {
        void invoke(ScrapperClient client, Long chatId);
    }

    @FunctionalInterface
    interface ResponseBodyProvider {
        Object getBody();
    }
}
