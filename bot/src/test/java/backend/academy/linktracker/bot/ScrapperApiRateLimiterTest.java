package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
            "resilience4j.ratelimiter.rate-limiter-aspect-order=1"
        })
@EnableWireMock
@DisplayName("Rate Limiter tests for ScrapperClient")
class ScrapperApiRateLimiterTest {

    private static final long TEST_CHAT_ID = 1L;
    private static final String TEST_LINK = "https://github.com/test/repo";
    private static final Set<String> TEST_TAGS = Set.of("test", "github");
    private static final Long TEST_LINK_ID = 1L;

    @Autowired
    private RateLimiterRegistry registry;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScrapperClient scrapperClient;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @AfterEach
    void cleanUp() {
        WireMock.reset();
    }

    @Test
    @DisplayName("Should allow requests within limit for getLinks")
    void shouldAllowRequestsWithinLimitForGetLinks() {
        RateLimiterConfig config = registry.rateLimiter("getLinksLimiter").getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        String successResponse = getGetLinksSuccessResponse();
        setupSuccessStub("/links", "GET", successResponse);

        for (int i = 0; i < limit; i++) {
            assertDoesNotThrow(
                    () -> scrapperClient.getLinks(TEST_CHAT_ID), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should reset rate limit after refresh period for getLinks")
    void shouldResetRateLimitAfterRefreshPeriodForGetLinks() {
        var rateLimiter = registry.rateLimiter("getLinksLimiter");
        WireMock.reset();

        RateLimiterConfig config = rateLimiter.getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        String successResponse = getGetLinksSuccessResponse();
        setupSuccessStub("/links", "GET", successResponse);

        for (int i = 0; i < limit; i++) {
            scrapperClient.getLinks(TEST_CHAT_ID);
        }

        assertThrows(RequestNotPermitted.class, () -> scrapperClient.getLinks(TEST_CHAT_ID));

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertDoesNotThrow(
                        () -> scrapperClient.getLinks(TEST_CHAT_ID), "Request should be allowed after refresh period"));
    }

    @Test
    @DisplayName("Should allow requests within limit for addLink")
    void shouldAllowRequestsWithinLimitForAddLink() {
        RateLimiterConfig config = registry.rateLimiter("addLinkLimiter").getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        String successResponse = getAddLinkSuccessResponse();
        setupSuccessStub("/links", "POST", successResponse);

        AddLinkRequest request = new AddLinkRequest(TEST_LINK, TEST_TAGS);

        for (int i = 0; i < limit; i++) {
            assertDoesNotThrow(
                    () -> scrapperClient.addLink(TEST_CHAT_ID, request), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should reject request when exceeding limit for addLink")
    void shouldRejectWhenExceedingLimitForAddLink() {
        RateLimiterConfig config = registry.rateLimiter("addLinkLimiter").getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        String successResponse = getAddLinkSuccessResponse();
        setupSuccessStub("/links", "POST", successResponse);

        AddLinkRequest request = new AddLinkRequest(TEST_LINK, TEST_TAGS);

        for (int i = 0; i < limit; i++) {
            scrapperClient.addLink(TEST_CHAT_ID, request);
        }

        assertThrows(
                RequestNotPermitted.class,
                () -> scrapperClient.addLink(TEST_CHAT_ID, request),
                "Request beyond limit should be rejected");
    }

    @Test
    @DisplayName("Should reset rate limit after refresh period for addLink")
    void shouldResetRateLimitAfterRefreshPeriodForAddLink() {
        var rateLimiter = registry.rateLimiter("addLinkLimiter");
        WireMock.reset();

        RateLimiterConfig config = rateLimiter.getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        String successResponse = getAddLinkSuccessResponse();
        setupSuccessStub("/links", "POST", successResponse);

        AddLinkRequest request = new AddLinkRequest(TEST_LINK, TEST_TAGS);

        for (int i = 0; i < limit; i++) {
            scrapperClient.addLink(TEST_CHAT_ID, request);
        }

        assertThrows(RequestNotPermitted.class, () -> scrapperClient.addLink(TEST_CHAT_ID, request));

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertDoesNotThrow(
                        () -> scrapperClient.addLink(TEST_CHAT_ID, request),
                        "Request should be allowed after refresh period"));
    }

    @Test
    @DisplayName("Should allow requests within limit for untrackLink")
    void shouldAllowRequestsWithinLimitForUntrackLink() {
        RateLimiterConfig config = registry.rateLimiter("untrackLinkLimiter").getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        String successResponse = getUntrackLinkSuccessResponse();
        setupSuccessStub("/links", "DELETE", successResponse);

        RemoveLinkRequest request = new RemoveLinkRequest(TEST_LINK);

        for (int i = 0; i < limit; i++) {
            assertDoesNotThrow(
                    () -> scrapperClient.untrackLink(TEST_CHAT_ID, request),
                    "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should reject request when exceeding limit for untrackLink")
    void shouldRejectWhenExceedingLimitForUntrackLink() {
        RateLimiterConfig config = registry.rateLimiter("untrackLinkLimiter").getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        String successResponse = getUntrackLinkSuccessResponse();
        setupSuccessStub("/links", "DELETE", successResponse);

        RemoveLinkRequest request = new RemoveLinkRequest(TEST_LINK);

        for (int i = 0; i < limit; i++) {
            scrapperClient.untrackLink(TEST_CHAT_ID, request);
        }

        assertThrows(
                RequestNotPermitted.class,
                () -> scrapperClient.untrackLink(TEST_CHAT_ID, request),
                "Request beyond limit should be rejected");
    }

    @Test
    @DisplayName("Should reset rate limit after refresh period for untrackLink")
    void shouldResetRateLimitAfterRefreshPeriodForUntrackLink() {
        var rateLimiter = registry.rateLimiter("untrackLinkLimiter");
        WireMock.reset();

        RateLimiterConfig config = rateLimiter.getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        String successResponse = getUntrackLinkSuccessResponse();
        setupSuccessStub("/links", "DELETE", successResponse);

        RemoveLinkRequest request = new RemoveLinkRequest(TEST_LINK);

        for (int i = 0; i < limit; i++) {
            scrapperClient.untrackLink(TEST_CHAT_ID, request);
        }

        assertThrows(RequestNotPermitted.class, () -> scrapperClient.untrackLink(TEST_CHAT_ID, request));

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertDoesNotThrow(
                        () -> scrapperClient.untrackLink(TEST_CHAT_ID, request),
                        "Request should be allowed after refresh period"));
    }

    @Test
    @DisplayName("Should allow requests within limit for registerChat")
    void shouldAllowRequestsWithinLimitForRegisterChat() {
        RateLimiterConfig config = registry.rateLimiter("registerChatLimiter").getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        setupSuccessStub("/tg-chat/" + TEST_CHAT_ID, "POST", "");

        for (int i = 0; i < limit; i++) {
            assertDoesNotThrow(
                    () -> scrapperClient.registerChat(TEST_CHAT_ID), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should allow requests within limit for deleteChat")
    void shouldAllowRequestsWithinLimitForDeleteChat() {
        RateLimiterConfig config = registry.rateLimiter("deleteChatLimiter").getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        setupSuccessStub("/tg-chat/" + TEST_CHAT_ID, "DELETE", "");

        for (int i = 0; i < limit; i++) {
            assertDoesNotThrow(
                    () -> scrapperClient.deleteChat(TEST_CHAT_ID), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should reject request when exceeding limit for deleteChat")
    void shouldRejectWhenExceedingLimitForDeleteChat() {
        RateLimiterConfig config = registry.rateLimiter("deleteChatLimiter").getRateLimiterConfig();
        int limit = config.getLimitForPeriod();

        setupSuccessStub("/tg-chat/" + TEST_CHAT_ID, "DELETE", "");

        for (int i = 0; i < limit; i++) {
            scrapperClient.deleteChat(TEST_CHAT_ID);
        }

        assertThrows(
                RequestNotPermitted.class,
                () -> scrapperClient.deleteChat(TEST_CHAT_ID),
                "Request beyond limit should be rejected");
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
        }
    }

    private String getGetLinksSuccessResponse() {
        ListLinksResponse response = new ListLinksResponse(List.of(), 0);
        return writeValueAsString(response);
    }

    private String getAddLinkSuccessResponse() {
        LinkResponse response = new LinkResponse(
                TEST_LINK_ID, URI.create(TEST_LINK), TEST_TAGS.stream().toList());
        return writeValueAsString(response);
    }

    private String getUntrackLinkSuccessResponse() {
        LinkResponse response = new LinkResponse(
                TEST_LINK_ID, URI.create(TEST_LINK), TEST_TAGS.stream().toList());
        return writeValueAsString(response);
    }

    private String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
