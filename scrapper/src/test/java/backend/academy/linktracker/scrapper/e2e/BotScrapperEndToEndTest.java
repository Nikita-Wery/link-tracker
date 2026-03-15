package backend.academy.linktracker.scrapper.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class BotScrapperEndToEndTest {

    static final DockerImageName BOT_IMAGE =
            DockerImageName.parse("bot:0.0.1");

    static final DockerImageName SCRAPPER_IMAGE =
            DockerImageName.parse("scrapper:0.0.1");

    @Container
    static GenericContainer<?> bot =
            new GenericContainer<>(BOT_IMAGE)
                    .withExposedPorts(8081)
                    .waitingFor(
                        Wait.forHttp("/actuator/health/liveness")
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofSeconds(30))
                    )
                    .withReuse(true);

    @Container
    static GenericContainer<?> scrapper =
            new GenericContainer<>(SCRAPPER_IMAGE)
                    .withExposedPorts(8080)
                    .waitingFor(
                        Wait.forHttp("/actuator/health/liveness")
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofSeconds(30))
                    )
                    .withReuse(true);

    @Test
    void botHealthTest() {
        var response = botClient()
                .get()
                .uri("/actuator/health/liveness")
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    private RestClient botClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + bot.getMappedPort(8081))
                .build();
    }
}
