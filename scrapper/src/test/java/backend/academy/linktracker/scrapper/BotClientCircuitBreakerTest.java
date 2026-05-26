package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.State.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.scrapper.client.inner.impl.RestBotClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.rest.RestBotClientConfiguration;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.bot.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.botexception.responsexception.BotServerException;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
        classes = {RestBotClientConfiguration.class, ObjectMapper.class},
        properties = {
            "spring.autoconfigure.exclude=" + "org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration",
            "app.client.bot.host=${wiremock.server.baseUrl}",
            "spring.jpa.hibernate.ddl-auto=none",
            "resilience4j.retry.instances.botClient.max-attempts=1",
            "resilience4j.circuitbreaker.circuit-breaker-aspect-order=1"
        })
@EnableWireMock
@ActiveProfiles("test")
@EnableConfigurationProperties(BotProperties.class)
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            LiquibaseAutoConfiguration.class
        })
@DisplayName("Circuit Breaker tests for BotClient")
class BotClientCircuitBreakerTest {

    private static final long TEST_CHAT_ID = 1L;
    private static final String TEST_URL = "https://github.com/test/repo";
    private static final String UPDATES_PATH = "/updates";

    @Autowired
    private CircuitBreakerRegistry registry;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestBotClient botClient;

    private CircuitBreaker circuitBreaker;
    private CircuitBreakerConfig config;

    @BeforeEach
    void setUp() {
        circuitBreaker = registry.circuitBreaker("botApi");
        config = circuitBreaker.getCircuitBreakerConfig();
    }

    @AfterEach
    void cleanUp() {
        circuitBreaker.reset();
        WireMock.reset();
    }

    @Test
    @DisplayName("Should reject requests with CallNotPermittedException when circuit breaker is OPEN")
    void shouldRejectRequestsWhenCircuitBreakerIsOpen() {

        int minCalls = config.getMinimumNumberOfCalls();
        String errorBody = createErrorResponse("BotServerException");

        openCircuitBreaker(errorBody, minCalls);

        long startTime = System.currentTimeMillis();

        assertThrows(CallNotPermittedException.class, () -> botClient.sendUpdate(createLinkUpdate()));

        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration < 100, "Request should be rejected immediately");
        verify(minCalls, postRequestedFor(urlEqualTo(UPDATES_PATH)));
    }

    @Test
    @DisplayName("Should transition from HALF_OPEN to CLOSED after successful requests")
    void shouldTransitionFromHalfOpenToClosedOnSuccess() {

        int minCalls = config.getMinimumNumberOfCalls();
        String errorBody = createErrorResponse("BotServerException");

        openCircuitBreaker(errorBody, minCalls);

        waitForState(HALF_OPEN);

        int permittedCalls = config.getPermittedNumberOfCallsInHalfOpenState();
        setupSuccessStub();

        for (int i = 0; i < permittedCalls; i++) {
            assertDoesNotThrow(() -> botClient.sendUpdate(createLinkUpdate()));
        }

        assertEquals(
                CLOSED,
                circuitBreaker.getState(),
                "Circuit breaker should close after successful calls in HALF_OPEN state");
    }

    @Test
    @DisplayName("Should return to OPEN state when failing in HALF_OPEN state")
    void shouldReturnToOpenWhenFailingInHalfOpen() {
        // Given
        int minCalls = config.getMinimumNumberOfCalls();
        String errorBody = createErrorResponse("BotServerException");

        openCircuitBreaker(errorBody, minCalls);

        waitForState(HALF_OPEN);

        int permittedCalls = config.getPermittedNumberOfCallsInHalfOpenState();
        setupErrorStub(errorBody);

        for (int i = 0; i < permittedCalls; i++) {
            assertThrows(BotServerException.class, () -> botClient.sendUpdate(createLinkUpdate()));
        }

        assertEquals(
                OPEN,
                circuitBreaker.getState(),
                "Circuit breaker should return to OPEN after failed calls in HALF_OPEN state");
    }

    private void openCircuitBreaker(String errorBody, int minCalls) {
        setupErrorStub(errorBody);

        for (int i = 0; i < minCalls; i++) {
            assertThrows(BotServerException.class, () -> botClient.sendUpdate(createLinkUpdate()));
        }

        assertEquals(OPEN, circuitBreaker.getState(), "Circuit breaker should be OPEN after " + minCalls + " failures");
        verify(minCalls, postRequestedFor(urlEqualTo(UPDATES_PATH)));
    }

    private void waitForState(CircuitBreaker.State expectedState) {
        long waitTime = config.getWaitIntervalFunctionInOpenState().apply(1);

        await().atMost(Duration.ofMillis(waitTime + 2000))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> circuitBreaker.getState() == expectedState);
    }

    private void setupErrorStub(String responseBody) {
        stubFor(post(urlEqualTo(UPDATES_PATH))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }

    private void setupSuccessStub() {
        stubFor(post(urlEqualTo(UPDATES_PATH)).willReturn(aResponse().withStatus(200)));
    }

    private String createErrorResponse(String exceptionName) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .description("Test error description")
                .code("TEST_ERROR")
                .exceptionName(exceptionName)
                .exceptionMessage("Test exception message")
                .stackTrace(List.of())
                .build();

        return writeValueAsString(error);
    }

    private LinkUpdate createLinkUpdate() {
        return new LinkUpdate(
                TEST_CHAT_ID,
                URI.create(TEST_URL),
                "Test update message",
                Set.of(TEST_CHAT_ID),
                ResourceType.GITHUB_REPOSITORY,
                OffsetDateTime.now());
    }

    private String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
}
