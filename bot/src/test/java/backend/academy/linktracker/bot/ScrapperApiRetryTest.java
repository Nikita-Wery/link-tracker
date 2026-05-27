package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.UnknownScrapperApiException;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
            "app.telegram.enabled=false",
            "app.client.scrapper.api.grpc.enabled=false",
        })
@EnableWireMock
@ActiveProfiles("retry-test")
class ScrapperApiRetryTest {

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScrapperClient scrapperClient;

    @Test
    void shouldRetryThreeTimes() {
        String errorResponseString = getErrorJsonString();
        String dtoString = getDtoJson();

        stubRetryScenario(errorResponseString, dtoString);

        System.out.println("All stubs: "
                + WireMock.listAllStubMappings().getMappings().stream()
                        .peek(s -> System.out.println("STUB NAME: " + s.getName())));

        assertDoesNotThrow(() -> scrapperClient.getLinks(1L));

        verify(3, getRequestedFor(urlEqualTo("/links")));
    }

    @Test
    void shouldNotRetry_whenServiceReturnsUnknownException() {
        String errorResponseString = getErrorJsonString();

        stubFor(get(urlEqualTo("/links"))
                .inScenario("retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponseString))
                .willSetStateTo("second"));

        stubFor(get(urlEqualTo("/links"))
                .inScenario("retry")
                .whenScenarioStateIs("second")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorResponseString)));

        assertThrows(UnknownScrapperApiException.class, () -> scrapperClient.getLinks(1L));

        verify(1, getRequestedFor(urlEqualTo("/links")));
    }

    @Test
    void shouldRetryEquallyInTime() {
        Retry retry = retryRegistry.retry("getLinksRetry");

        List<Long> millis = new ArrayList<>();

        retry.getEventPublisher().onRetry(e -> millis.add(System.currentTimeMillis()));

        String errorResponseString = getErrorJsonString();
        String dtoString = getDtoJson();

        stubRetryScenario(errorResponseString, dtoString);

        long expectedBackoffMs = retry.getRetryConfig().getIntervalBiFunction().apply(1, null);

        long msError = Math.round(expectedBackoffMs * 0.2);

        assertDoesNotThrow(() -> scrapperClient.getLinks(1L));

        assertFalse(millis.isEmpty());

        System.out.println(expectedBackoffMs + " " + msError);

        for (int i = 0; i < millis.size() - 1; i++) {
            long duration = millis.get(i + 1) - millis.get(i);

            System.out.println(duration);
            assertTrue(((expectedBackoffMs - msError) < duration) && (duration < (expectedBackoffMs + msError)));
        }
    }

    private String getErrorJsonString() {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .description("test")
                .code("test")
                .exceptionName("RetryableException")
                .exceptionMessage("test")
                .stackTrace(List.of())
                .build();

        return objectMapper.writeValueAsString(error);
    }

    private String getDtoJson() {
        ListLinksResponse dto = new ListLinksResponse(List.of(), 0);
        return objectMapper.writeValueAsString(dto);
    }

    private void stubRetryScenario(String errorBody, String okBody) {
        stubFor(get(urlEqualTo("/links"))
                .inScenario("retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorBody))
                .willSetStateTo("second"));

        stubFor(get(urlEqualTo("/links"))
                .inScenario("retry")
                .whenScenarioStateIs("second")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorBody))
                .willSetStateTo("third"));

        stubFor(get(urlEqualTo("/links"))
                .inScenario("retry")
                .whenScenarioStateIs("third")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(okBody)));
    }
}
