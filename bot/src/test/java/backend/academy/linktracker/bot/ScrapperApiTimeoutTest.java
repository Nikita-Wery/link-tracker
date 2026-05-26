package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.dto.ApiErrorResponse;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.UnknownScrapperApiException;
import backend.academy.linktracker.bot.properties.ScrapperProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.ObjectMapper;

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
@Testcontainers
@EnableWireMock
public class ScrapperApiTimeoutTest {

    @Autowired
    private ScrapperProperties scrapperProperties;

    private static final Long CHAT_ID = 1L;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScrapperClient scrapperClient;

    @Test
    void shouldRetryGetLinksThreeTimesAndSucceed() throws Exception {
        stubRetryScenario(
                "retry-get-links", () -> get(urlEqualTo("/links")), getErrorResponseJson(), getLinksResponseJson());

        assertDoesNotThrow(() -> scrapperClient.getLinks(CHAT_ID));

        verify(3, getRequestedFor(urlEqualTo("/links")));
    }

    @Test
    void shouldNotRetryGetLinksForUnknownClientException() throws Exception {
        stubClientErrorScenario(
                "retry-get-links-client-error", () -> get(urlEqualTo("/links")), getErrorResponseJson());

        assertThrows(UnknownScrapperApiException.class, () -> scrapperClient.getLinks(CHAT_ID));

        verify(1, getRequestedFor(urlEqualTo("/links")));
    }

    @Test
    void shouldRetryGetLinksWithConfiguredBackoff() throws Exception {
        Retry retry = retryRegistry.retry("getLinksRetry");

        List<Long> retryMoments = new ArrayList<>();

        retry.getEventPublisher().onRetry(event -> retryMoments.add(System.currentTimeMillis()));

        stubRetryScenario(
                "retry-get-links-backoff",
                () -> get(urlEqualTo("/links")),
                getErrorResponseJson(),
                getLinksResponseJson());

        long expectedDelay = retry.getRetryConfig().getIntervalBiFunction().apply(1, null);

        long allowedDelta = Math.round(expectedDelay * 0.2);

        assertDoesNotThrow(() -> scrapperClient.getLinks(CHAT_ID));

        assertFalse(retryMoments.isEmpty());

        for (int i = 0; i < retryMoments.size() - 1; i++) {
            long actualDelay = retryMoments.get(i + 1) - retryMoments.get(i);

            assertTrue(actualDelay >= expectedDelay - allowedDelta && actualDelay <= expectedDelay + allowedDelta);
        }
    }

    @Test
    void shouldRetryRegisterChatThreeTimesAndSucceed() throws Exception {
        stubRetryScenarioWithoutBody("retry-register-chat", () -> post(urlEqualTo("/tg-chat/1")));

        assertDoesNotThrow(() -> scrapperClient.registerChat(CHAT_ID));

        verify(3, postRequestedFor(urlEqualTo("/tg-chat/1")));
    }

    @Test
    void shouldRetryDeleteChatTwoTimesAndSucceed() throws Exception {
        stubRetryScenarioWithoutBodyTwoAttempts("retry-delete-chat", () -> delete(urlEqualTo("/tg-chat/1")));

        assertDoesNotThrow(() -> scrapperClient.deleteChat(CHAT_ID));

        verify(2, deleteRequestedFor(urlEqualTo("/tg-chat/1")));
    }

    @Test
    void shouldRetryAddLinkThreeTimesAndSucceed() throws Exception {
        AddLinkRequest request = new AddLinkRequest("https://github.com/torvalds/linux", Set.of("tag"));

        stubRetryScenario(
                "retry-add-link", () -> post(urlEqualTo("/links")), getErrorResponseJson(), getLinkResponseJson());

        assertDoesNotThrow(() -> scrapperClient.addLink(CHAT_ID, request));

        verify(3, postRequestedFor(urlEqualTo("/links")));
    }

    @Test
    void shouldRetryUntrackLinkTwoTimesAndSucceed() throws Exception {
        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/torvalds/linux");

        stubRetryScenarioTwoAttempts(
                "retry-untrack-link",
                () -> delete(urlEqualTo("/links")),
                getErrorResponseJson(),
                getLinkResponseJson());

        assertDoesNotThrow(() -> scrapperClient.untrackLink(CHAT_ID, request));

        verify(2, deleteRequestedFor(urlEqualTo("/links")));
    }

    private String getErrorResponseJson() throws JsonProcessingException {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .description("test")
                .code("test")
                .exceptionName("RetryableException")
                .exceptionMessage("test")
                .stackTrace(List.of())
                .build();

        return objectMapper.writeValueAsString(error);
    }

    private String getLinksResponseJson() throws JsonProcessingException {
        ListLinksResponse response = new ListLinksResponse(List.of(), 0);

        return objectMapper.writeValueAsString(response);
    }

    private String getLinkResponseJson() {
        LinkResponse response = new LinkResponse(1L, URI.create("https://github.com/torvalds/linux"), List.of("tag"));

        return objectMapper.writeValueAsString(response);
    }

    private void stubClientErrorScenario(
            String scenarioName, Supplier<MappingBuilder> mappingBuilder, String errorBody) {

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorBody)));
    }

    private void stubRetryScenario(
            String scenarioName, Supplier<MappingBuilder> mappingBuilder, String errorBody, String successBody) {

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorBody))
                .willSetStateTo("second"));

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs("second")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorBody))
                .willSetStateTo("third"));

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs("third")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(successBody)));
    }

    private void stubRetryScenarioTwoAttempts(
            String scenarioName, Supplier<MappingBuilder> mappingBuilder, String errorBody, String successBody) {

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody(errorBody))
                .willSetStateTo("second"));

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs("second")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(successBody)));
    }

    private void stubRetryScenarioWithoutBody(String scenarioName, Supplier<MappingBuilder> mappingBuilder) {

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("second"));

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs("second")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("third"));

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs("third")
                .willReturn(aResponse().withStatus(200)));
    }

    private void stubRetryScenarioWithoutBodyTwoAttempts(String scenarioName, Supplier<MappingBuilder> mappingBuilder) {

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("second"));

        stubFor(mappingBuilder
                .get()
                .inScenario(scenarioName)
                .whenScenarioStateIs("second")
                .willReturn(aResponse().withStatus(200)));
    }
}
