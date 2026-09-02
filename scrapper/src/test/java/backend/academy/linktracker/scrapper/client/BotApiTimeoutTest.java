package backend.academy.linktracker.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.client.inner.BotClient;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.config.scrapperconfiguration.api.rest.RestBotClientConfiguration;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.properties.BotProperties;
import backend.academy.linktracker.scrapper.service.logs.ScrapperMetricsService;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
        classes = {
            RestBotClientConfiguration.class,
            ObjectMapper.class
        },
        properties = {"app.client.scrapper.base-url=${wiremock.server.baseUrl}"})
@EnableWireMock
@ActiveProfiles("test")
@EnableConfigurationProperties(BotProperties.class)
public class BotApiTimeoutTest {

    @Autowired
    private BotProperties botProperties;

    @Autowired
    private BotClient botClient;

    @MockitoBean
    ScrapperMetricsService scrapperMetricsService;

    @Test
    void shouldThrowTimeoutException() {
        long timeoutMillis = botProperties.getTimeout().getRead().toMillis();
        int fixedDelay = (int) timeoutMillis + 1000;

        stubFor(post(urlEqualTo("/updates"))
                .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withFixedDelay(fixedDelay)
                            .withBody("OK")));

        LinkUpdate linkUpdateDto = new LinkUpdate(
                1L,
                URI.create("https://github.com/torvalds/linux"),
                "descr",
                Set.of(1L),
                ResourceType.GITHUB_REPOSITORY,
                OffsetDateTime.now());

        long start = System.currentTimeMillis();

        assertThrows(ResourceAccessException.class, () -> botClient.sendUpdate(linkUpdateDto));

        long duration = System.currentTimeMillis() - start;

        assertTrue(duration < fixedDelay);
    }
}
