package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.client.responsehandler.APIBadResponseHandler;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.rest.RestExternalClientsConfiguration;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.github.GithubRepositoryUpdateTime;
import backend.academy.linktracker.scrapper.parser.impl.github.GithubRepositoryLinkParser;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import backend.academy.linktracker.scrapper.service.source.github.GithubRepositorySource;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
        classes = {
            GithubRepositorySource.class,
            ObjectMapper.class,
            RetryRegistry.class,
            RestExternalClientsConfiguration.class,
            APIBadResponseHandler.class,
            GithubProperties.class,
            StackoverflowProperties.class,
            GithubRepositoryLinkParser.class,
            WireMockServer.class
        },
        properties = {
            "spring.autoconfigure.exclude=" + "org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration",
            "app.client.github.host=${wiremock.server.baseUrl}",
            "app.client.github.token=token"
        })
@EnableWireMock
@ActiveProfiles({"test"})
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            LiquibaseAutoConfiguration.class
        })
class GithubRepositorySourceRetryTest {

    @Value("${app.client.github.host}")
    String host;

    @Autowired
    private GithubRepositorySource source;

    @Autowired
    private GithubProperties githubProperties;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRetryThreeTimesAndSucceed_andAfterInvokeFallback() throws Exception {

        System.out.println("HOST: " + host);
        System.out.println("PROPERTIES" + githubProperties.getHost() + " TOKEN: " + githubProperties.getToken());

        Link link = githubLink();

        stubGithubRetryScenario(githubErrorJson(), githubSuccessJson());

        List<LinkUpdate> updates = source.getUpdates(link);

        System.out.println("AFTER CALL");

        verify(3, getRequestedFor(urlEqualTo("/repos/test/repo")));

        assertFalse(updates.isEmpty());
    }

    @Test
    void shouldNotRetryForBadRequest_fallbackWorking() {

        stubFor(get(urlEqualTo("/repos/test/repo"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(githubErrorJson())));

        Link link = githubLink();

        assertDoesNotThrow(() -> source.getUpdates(link));

        verify(1, getRequestedFor(urlEqualTo("/repos/test/repo")));
    }

    @Test
    void shouldRetryWithCorrectBackoff() throws Exception {

        Retry retry = retryRegistry.retry("githubRepositoryRetry");

        List<Long> millis = new ArrayList<>();

        retry.getEventPublisher().onRetry(event -> millis.add(System.currentTimeMillis()));

        stubGithubRetryScenario(githubErrorJson(), githubSuccessJson());

        source.getUpdates(githubLink());

        long expected = retry.getRetryConfig().getIntervalBiFunction().apply(1, null);

        long error = Math.round(expected * 0.3);

        for (int i = 0; i < millis.size() - 1; i++) {

            long duration = millis.get(i + 1) - millis.get(i);

            assertTrue(duration >= expected - error && duration <= expected + error);
        }
    }

    private void stubGithubRetryScenario(String errorBody, String okBody) {

        stubFor(get(urlEqualTo("/repos/test/repo"))
                .inScenario("github-retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500).withBody(errorBody))
                .willSetStateTo("second"));

        stubFor(get(urlEqualTo("/repos/test/repo"))
                .inScenario("github-retry")
                .whenScenarioStateIs("second")
                .willReturn(aResponse().withStatus(500).withBody(errorBody))
                .willSetStateTo("third"));

        stubFor(get(urlEqualTo("/repos/test/repo"))
                .inScenario("github-retry")
                .whenScenarioStateIs("third")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(okBody)));
    }

    private String githubSuccessJson() throws Exception {

        GithubRepositoryUpdateTime dto =
                new GithubRepositoryUpdateTime("repo", OffsetDateTime.now(), OffsetDateTime.now());

        return objectMapper.writeValueAsString(dto);
    }

    private String githubErrorJson() {
        return """
               {
                 "message":"server error"
               }
               """;
    }

    private Link githubLink() {

        return new Link(
                "https://github.com/test/repo",
                ResourceType.GITHUB_REPOSITORY,
                OffsetDateTime.now().minusDays(1));
    }
}
