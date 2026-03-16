package backend.academy.linktracker.scrapper.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class BotScrapperEndToEndTest {

    static final DockerImageName BOT_IMAGE = DockerImageName.parse("bot:0.0.1");

    static final DockerImageName SCRAPPER_IMAGE = DockerImageName.parse("scrapper:0.0.1");

    @Container
    static GenericContainer<?> bot = new GenericContainer<>(BOT_IMAGE)
            .withExposedPorts(8080)
            .withEnv("APP_LOGGER_FILE_ENABLED", "false")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).withStartupTimeout(Duration.ofMinutes(1)));

    @Container
    static GenericContainer<?> scrapper = new GenericContainer<>(SCRAPPER_IMAGE)
            .withExposedPorts(8081)
            .withEnv("APP_LINK_VERIFICATION_ENABLED", "false")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).withStartupTimeout(Duration.ofMinutes(2)));

    @Test
    void botHealthTest() {
        var response =
                botClient().get().uri("/actuator/health/liveness").retrieve().toEntity(String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    private RestClient botClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + bot.getMappedPort(8080))
                .build();
    }
}
