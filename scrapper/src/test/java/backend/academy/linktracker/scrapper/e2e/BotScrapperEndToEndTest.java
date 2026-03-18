package backend.academy.linktracker.scrapper.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BotScrapperEndToEndTest {

    static final DockerImageName BOT_IMAGE = DockerImageName.parse("bot:0.0.1");

    static final DockerImageName SCRAPPER_IMAGE = DockerImageName.parse("scrapper:0.0.1");

    @Container
    static GenericContainer<?> bot = new GenericContainer<>(BOT_IMAGE)
            .withExposedPorts(8080)
            .withEnv("APP_LOGGER_FILE_ENABLED", "false")
            .withEnv("APP_TELEGRAM_ENABLED", "false")
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).withStartupTimeout(Duration.ofMinutes(2)));

    @Container
    static GenericContainer<?> scrapper = new GenericContainer<>(SCRAPPER_IMAGE)
            .withExposedPorts(8081)
            .withEnv("SPRING_TASK_SCHEDULING_ENABLED", "false")
            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
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

    private RestClient scrapperClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + scrapper.getMappedPort(8081))
                .defaultHeader("Tg-Chat-Id")
                .build();
    }

    @Test
    @Order(1)
    void bot_updates_returns200() {

        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("https://github.com/torvalds/linux"),
                "github repo update",
                Set.of(1L, 2L, 3L),
                ResourceType.GITHUB_REPOSITORY,
                OffsetDateTime.now());

        var response = botClient()
                .post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkUpdate)
                .retrieve()
                .toBodilessEntity();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @Order(2)
    void bot_updates_returns400() {

        String linkUpdateBody = """
            {
              "id": 1,
              "url": "https://github.com/torvalds/linux",
              "description": "github repo update"
            }
            """;

        HttpClientErrorException.BadRequest ex =
                assertThrows(HttpClientErrorException.BadRequest.class, () -> botClient()
                        .post()
                        .uri("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(linkUpdateBody)
                        .retrieve()
                        .toBodilessEntity());

        assertEquals(400, ex.getStatusCode().value());
    }

    @SneakyThrows
    @Test
    @Order(3)
    void scrapper_addAndGetLink() {
        long chatId = 1L;
        String url = "https://github.com/user/repo";

        var reg =
                scrapperClient().post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
        assertEquals(200, reg.getStatusCode().value());

        AddLinkRequest body = new AddLinkRequest(url, Collections.emptyList(), Collections.emptyList());

        var add = scrapperClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(body)
                .retrieve()
                .toBodilessEntity();

        assertEquals(200, add.getStatusCode().value());

        var resp = scrapperClient()
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains(url));
    }

    @Test
    @Order(4)
    void scrapper_addAndDeleteLink() {
        long chatId = 2L;
        String url = "https://github.com/user/repo2";

        scrapperClient().post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();

        AddLinkRequest body = new AddLinkRequest(url, Collections.emptyList(), Collections.emptyList());

        scrapperClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        RemoveLinkRequest removeBody = new RemoveLinkRequest(url);

        var del = scrapperClient()
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(removeBody)
                .retrieve()
                .toBodilessEntity();

        assertEquals(200, del.getStatusCode().value());

        var resp = scrapperClient()
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().contains(url));
    }

    @Test
    @Order(5)
    void scrapper_deleteFromUnknownChat_LinkStillExists() {
        long existingChatId = 3L;
        long notExistingChatId = 404L;

        String url = "https://github.com/user/repo3";

        scrapperClient().post().uri("/tg-chat/{id}", existingChatId).retrieve().toBodilessEntity();

        AddLinkRequest addBody = new AddLinkRequest(url, Collections.emptyList(), Collections.emptyList());

        scrapperClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(existingChatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBody)
                .retrieve()
                .toBodilessEntity();

        RemoveLinkRequest removeBody = new RemoveLinkRequest(url);

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> scrapperClient()
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(notExistingChatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(removeBody)
                .retrieve()
                .toBodilessEntity());

        assertTrue(ex.getStatusCode().is4xxClientError());

        var resp = scrapperClient()
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(existingChatId))
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains(url));
    }

    @Test
    @Order(6)
    void scrapper_addLinkToUnknownChat_return200() {
        long existingChatId = 4L;
        long firstTimeUseChatId = 5L;
        String url = "https://github.com/user/repo4";

        AddLinkRequest addBody = new AddLinkRequest(url, Collections.emptyList(), Collections.emptyList());

        scrapperClient().post().uri("/tg-chat/{id}", existingChatId).retrieve().toBodilessEntity();

        scrapperClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(existingChatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBody)
                .retrieve()
                .toEntity(String.class);

        var resp = scrapperClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(firstTimeUseChatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBody)
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains(url));
    }

    @Test
    @Order(7)
    void scrapper_ClientCanNotReattachLink_returns409() {
        long chatId = 6L;
        String url = "https://github.com/user/repo5";

        scrapperClient().post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();

        AddLinkRequest addBody = new AddLinkRequest(url, Collections.emptyList(), Collections.emptyList());

        var resp = scrapperClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBody)
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().contains(url));

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> scrapperClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBody)
                .retrieve()
                .toEntity(String.class));

        assertEquals(ex.getStatusCode(), HttpStatus.CONFLICT);
    }

    @Test
    @Order(8)
    void scrapper_deleteNonExistentChat_returns404() {
        long chatId = 404L;

        HttpClientErrorException.NotFound ex =
                assertThrows(HttpClientErrorException.NotFound.class, () -> scrapperClient()
                        .delete()
                        .uri("/tg-chat/{id}", chatId)
                        .retrieve()
                        .toBodilessEntity());

        assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
    }
}
