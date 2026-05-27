package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.scrapper.client.inner.impl.RestBotClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.rest.RestBotClientConfiguration;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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

@Tag("integration")
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
            "resilience4j.ratelimiter.instances.sendUpdateLimiter.timeout-duration=0s",
            "resilience4j.ratelimiter.rate-limiter-aspect-order=1"
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
@DisplayName("Rate Limiter tests for BotClient")
class BotClientRateLimiterTest {

    private static final long TEST_CHAT_ID = 1L;
    private static final String TEST_URL = "https://github.com/test/repo";
    private static final String UPDATES_PATH = "/updates";

    @Autowired
    private RateLimiterRegistry registry;

    @Autowired
    private RestBotClient botClient;

    @Test
    void shouldThrowException_whenTooManyRequests() {
        RateLimiterConfig config = registry.rateLimiter("sendUpdateLimiter").getRateLimiterConfig();

        int limitForPeriod = config.getLimitForPeriod();

        for (int i = 0; i < limitForPeriod; i++) {
            stubFor(post(urlEqualTo("/updates")).willReturn(aResponse().withStatus(200)));
        }

        LinkUpdate dto = getDto();
        for (int i = 0; i < limitForPeriod; i++) {
            assertDoesNotThrow(() -> botClient.sendUpdate(dto));
        }

        assertThrows(RequestNotPermitted.class, () -> botClient.sendUpdate(dto));
    }

    private LinkUpdate getDto() {
        return new LinkUpdate(
                TEST_CHAT_ID,
                URI.create(TEST_URL),
                "Test update message",
                Set.of(TEST_CHAT_ID),
                ResourceType.GITHUB_REPOSITORY,
                OffsetDateTime.now());
    }
}
