package backend.academy.linktracker.scrapper.e2e;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.linktracker.proto.BotServiceGrpc;
import backend.academy.linktracker.proto.GetLinksRequest;
import backend.academy.linktracker.proto.RegisterChatRequest;
import backend.academy.linktracker.proto.RemoveChatRequest;
import backend.academy.linktracker.proto.ScrapperServiceGrpc;
import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BotScrapperEndToEndTest {

    final DockerImageName BOT_IMAGE = DockerImageName.parse("bot:0.0.1");
    final DockerImageName SCRAPPER_IMAGE = DockerImageName.parse("scrapper:0.0.1");
    final DockerImageName LIQUIBASE_IMAGE = DockerImageName.parse("liquibase/liquibase:latest-alpine");

    final String SCRAPPER_MIGRATIONS_PATH = Path.of("")
            .toAbsolutePath()
            .getParent()
            .resolve("migrations/migrations-scrapper")
            .toString();

    final String BOT_MIGRATIONS_PATH = Path.of("")
            .toAbsolutePath()
            .getParent()
            .resolve("migrations/migrations-bot")
            .toString();

    Network network = Network.newNetwork();

    GenericContainer<?> bot;

    GenericContainer<?> scrapper;

    PostgreSQLContainer scrapperPostgreSQLContainer;

    PostgreSQLContainer botPostgreSQLContainer;

    GenericContainer<?> scrapperLiquibaseContainer;

    GenericContainer<?> botLiquibaseContainer;

    ManagedChannel botChannel;
    ManagedChannel scrapperChannel;

    @BeforeAll
    void upContainers() {

        scrapperPostgreSQLContainer = new PostgreSQLContainer("postgres:18-alpine")
                .withDatabaseName("linktracker_db")
                .withUsername("test")
                .withPassword("test")
                .withNetwork(network)
                .withNetworkAliases("scrapper-postgres")
                .waitingFor(Wait.forListeningPort());

        botPostgreSQLContainer = new PostgreSQLContainer("postgres:18-alpine")
                .withDatabaseName("bot_db")
                .withUsername("test")
                .withPassword("test")
                .withNetwork(network)
                .withNetworkAliases("bot-postgres")
                .waitingFor(Wait.forListeningPort());

        scrapperPostgreSQLContainer.start();

        botPostgreSQLContainer.start();

        scrapperLiquibaseContainer = new GenericContainer<>(LIQUIBASE_IMAGE)
                .withNetwork(network)
                .withFileSystemBind(SCRAPPER_MIGRATIONS_PATH, "/liquibase/changelog", BindMode.READ_ONLY)
                .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
                .withCommand(
                        "--url=jdbc:postgresql://scrapper-postgres:5432/linktracker_db",
                        "--username=test",
                        "--password=test",
                        "--changeLogFile=changelog-root.yaml",
                        "update")
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy());

        scrapperLiquibaseContainer.start();

        botLiquibaseContainer = new GenericContainer<>(LIQUIBASE_IMAGE)
                .withNetwork(network)
                .withFileSystemBind(BOT_MIGRATIONS_PATH, "/liquibase/changelog", BindMode.READ_ONLY)
                .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
                .withCommand(
                        "--url=jdbc:postgresql://bot-postgres:5432/bot_db",
                        "--username=test",
                        "--password=test",
                        "--changeLogFile=changelog-root.yaml",
                        "update")
                .withStartupCheckStrategy(new OneShotStartupCheckStrategy());

        botLiquibaseContainer.start();

        bot = new GenericContainer<>(BOT_IMAGE)
                .withExposedPorts(8080, 9090)
                .withEnv("APP_LOGGER_FILE_ENABLED", "false")
                .withEnv("APP_TELEGRAM_ENABLED", "false")
                .withEnv("APP_CLIENT_SCRAPPER_API_KAFKA_ENABLED", "false")
                .withEnv("APP_CLIENT_SCRAPPER_API_GRPC_ENABLED", "true")
                .withEnv("APP_CLIENT_SCRAPPER_API_REST_ENABLED", "true")
                .withEnv("APP_CLIENT_SCRAPPER_HOST", "http://scrapper:8081")
                .withEnv("APP_CLIENT_SCRAPPER_GRPC_HOST", "scrapper:9091")
                .withEnv("DB_HOST", "bot-postgres")
                .withEnv("DB_PORT", "5432")
                .withEnv("DB_NAME", "bot_db")
                .withEnv("DB_USERNAME", "test")
                .withEnv("DB_PASSWORD", "test")
//                .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
                .withNetwork(network)
                .withNetworkAliases("bot")
                .waitingFor(Wait.forHttp("/actuator/health").forPort(8080).withStartupTimeout(Duration.ofMinutes(2)));

        bot.start();

        scrapper = new GenericContainer<>(SCRAPPER_IMAGE)
                .withExposedPorts(8081, 9091)
                .withEnv("SPRING_TASK_SCHEDULING_ENABLED", "false")
                .withEnv("APP_CLIENT_BOT_API_REST_ENABLED", "true")
                .withEnv("APP_CLIENT_BOT_API_GRPC_ENABLED", "true")
                .withEnv("APP_CLIENT_BOT_API_KAFKA_ENABLED", "false")
                .withEnv("APP_DB_ACCESS_TYPE", "orm")
                .withEnv("APP_CLIENT_BOT_HOST", "http://bot:8080")
                .withEnv("APP_CLIENT_BOT_GRPC_HOST", "bot:9090")
                .withEnv("DB_HOST", "scrapper-postgres")
                .withEnv("DB_PORT", "5432")
                .withEnv("DB_NAME", "linktracker_db")
                .withEnv("DB_USERNAME", "test")
                .withEnv("DB_PASSWORD", "test")
                .withEnv("SPRING_LIQUIBASE_ENABLED", "false")
//                .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
                .withNetwork(network)
                .withNetworkAliases("scrapper")
                .waitingFor(Wait.forHttp("/actuator/health").forPort(8081).withStartupTimeout(Duration.ofMinutes(2)));

        scrapper.start();
        initGrpc();
    }

    @AfterAll
    void shutdown() throws InterruptedException {
        if (botChannel != null) {
            botChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }

        if (scrapperChannel != null) {
            scrapperChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public void initGrpc() {
        botChannel = ManagedChannelBuilder.forAddress("localhost", bot.getMappedPort(9090))
                .usePlaintext()
                .build();

        scrapperChannel = ManagedChannelBuilder.forAddress("localhost", scrapper.getMappedPort(9091))
                .usePlaintext()
                .build();
    }

    private RestClient botRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + bot.getMappedPort(8080))
                .build();
    }

    private RestClient scrapperRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + scrapper.getMappedPort(8081))
                .defaultHeader("Tg-Chat-Id")
                .build();
    }

    private BotServiceGrpc.BotServiceBlockingStub botGrpcClient() {
        return BotServiceGrpc.newBlockingStub(botChannel);
    }

    private ScrapperServiceGrpc.ScrapperServiceBlockingStub scrapperGrpcClient() {
        return ScrapperServiceGrpc.newBlockingStub(scrapperChannel);
    }

    private HealthGrpc.HealthBlockingStub grpcHealthStub() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", scrapper.getMappedPort(9091))
                .usePlaintext()
                .build();

        return HealthGrpc.newBlockingStub(channel);
    }

    @Test
    void restBotHealthTest() {
        var response = botRestClient()
                .get()
                .uri("/actuator/health/liveness")
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void grpc_healthTest() {
        HealthCheckRequest request =
                HealthCheckRequest.newBuilder().setService("").build();

        HealthCheckResponse response = grpcHealthStub().check(request);

        assertEquals(HealthCheckResponse.ServingStatus.SERVING, response.getStatus());
    }

    @Test
    @Order(1)
    void rest_bot_updates_returns200() {

        LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("https://github.com/torvalds/linux"),
                "github repo update",
                Set.of(1L, 2L, 3L),
                ResourceType.GITHUB_REPOSITORY,
                OffsetDateTime.now());

        var response = botRestClient()
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
    void grpc_bot_updates_notThrow() {

        var stub = botGrpcClient();

        assertDoesNotThrow(() -> stub.sendUpdate(backend.academy.linktracker.proto.LinkUpdate.newBuilder()
                .setId(2L)
                .setUrl("https://github.com/torvalds/linux")
                .setDescription("github repo update")
                .addTgChatIds(2L)
                .addTgChatIds(3L)
                .addTgChatIds(4L)
                .build()));
    }

    @Test
    @Order(3)
    void bot_updates_returns400() {

        String linkUpdateBody = """
            {
              "id": 1,
              "url": "https://github.com/torvalds/linux",
              "description": "github repo update"
            }
            """;

        HttpClientErrorException.BadRequest ex =
                assertThrows(HttpClientErrorException.BadRequest.class, () -> botRestClient()
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
    @Order(4)
    void scrapper_addAndGetLink() {
        long chatId = 1L;
        String url = "https://github.com/user/repo";

        var reg = scrapperRestClient()
                .post()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .toBodilessEntity();

        assertEquals(200, reg.getStatusCode().value());

        AddLinkRequest body = new AddLinkRequest(url, Collections.emptyList());

        var add = scrapperRestClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(body)
                .retrieve()
                .toBodilessEntity();

        assertEquals(200, add.getStatusCode().value());

        var resp = scrapperRestClient()
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        log.info("BODY: {}", resp.getBody());
        assertTrue(resp.getBody().contains(url));
    }

    @SneakyThrows
    @Test
    @Order(5)
    void grpc_scrapper_addAndGetLink() {
        long chatId = 5L;
        String url = "https://github.com/user/repo";

        var stub = scrapperGrpcClient();

        var req = stub.registerChat(
                RegisterChatRequest.newBuilder().setChatId(chatId).build());

        backend.academy.linktracker.proto.AddLinkRequest body =
                backend.academy.linktracker.proto.AddLinkRequest.newBuilder()
                        .setChatId(chatId)
                        .setUrl(url)
                        .addTags("tag")
                        .build();

        var add = stub.addLink(body);

        assertEquals(url, add.getUrl());

        var resp = stub.getLinks(GetLinksRequest.newBuilder().setChatId(chatId).build());

        assertEquals(1, resp.getSize());
    }

    @Test
    @Order(6)
    void scrapper_addAndDeleteLink() {
        long chatId = 2L;
        String url = "https://github.com/user/repo2";

        scrapperRestClient().post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();

        AddLinkRequest body = new AddLinkRequest(url, Collections.emptyList());

        scrapperRestClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        RemoveLinkRequest removeBody = new RemoveLinkRequest(url);

        var del = scrapperRestClient()
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(removeBody)
                .retrieve()
                .toBodilessEntity();

        assertEquals(200, del.getStatusCode().value());

        var resp = scrapperRestClient()
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
    @Order(7)
    void grpc_scrapper_addAndDeleteLink() {
        long chatId = 2L;
        String url = "https://github.com/user/repo2";

        var stub = scrapperGrpcClient();

        //        var req = stub.registerChat(
        //                RegisterChatRequest.newBuilder().setChatId(chatId).build());

        backend.academy.linktracker.proto.AddLinkRequest body =
                backend.academy.linktracker.proto.AddLinkRequest.newBuilder()
                        .setChatId(chatId)
                        .setUrl(url)
                        .addTags("tag")
                        .build();

        var add = stub.addLink(body);

        backend.academy.linktracker.proto.RemoveLinkRequest request =
                backend.academy.linktracker.proto.RemoveLinkRequest.newBuilder()
                        .setChatId(chatId)
                        .setLink(url)
                        .build();

        var del = stub.deleteLink(request);

        assertEquals(url, del.getUrl());

        var resp = stub.getLinks(GetLinksRequest.newBuilder().setChatId(chatId).build());

        assertEquals(0, resp.getSize());
    }

    @Test
    @Order(8)
    void scrapper_deleteFromUnknownChat_LinkStillExists() {
        long existingChatId = 3L;
        long notExistingChatId = 404L;

        String url = "https://github.com/user/repo3";

        scrapperRestClient()
                .post()
                .uri("/tg-chat/{id}", existingChatId)
                .retrieve()
                .toBodilessEntity();

        AddLinkRequest addBody = new AddLinkRequest(url, Collections.emptyList());

        scrapperRestClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(existingChatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBody)
                .retrieve()
                .toBodilessEntity();

        RemoveLinkRequest removeBody = new RemoveLinkRequest(url);

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> scrapperRestClient()
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(notExistingChatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(removeBody)
                .retrieve()
                .toBodilessEntity());

        assertTrue(ex.getStatusCode().is4xxClientError());

        var resp = scrapperRestClient()
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
    @Order(9)
    void grpc_scrapper_deleteFromUnknownChat_throwStatusRuntime() {
        long existingChatId = 7L;
        long notExistingChatId = 404L;

        String url = "https://github.com/user/repo4";

        var stub = scrapperGrpcClient();

        backend.academy.linktracker.proto.AddLinkRequest body =
                backend.academy.linktracker.proto.AddLinkRequest.newBuilder()
                        .setChatId(existingChatId)
                        .setUrl(url)
                        .addTags("tag")
                        .build();

        var add = stub.addLink(body);

        backend.academy.linktracker.proto.RemoveLinkRequest delRequest =
                backend.academy.linktracker.proto.RemoveLinkRequest.newBuilder()
                        .setChatId(notExistingChatId)
                        .setLink(url)
                        .build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () -> stub.deleteLink(delRequest));
    }

    @Test
    @Order(10)
    void scrapper_addLinkToUnknownChat_return200() {
        long existingChatId = 8L;
        long firstTimeUseChatId = 9L;
        String url = "https://github.com/user/repo5";

        AddLinkRequest addBody = new AddLinkRequest(url, Collections.emptyList());

        scrapperRestClient()
                .post()
                .uri("/tg-chat/{id}", existingChatId)
                .retrieve()
                .toBodilessEntity();

        scrapperRestClient()
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(existingChatId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBody)
                .retrieve()
                .toEntity(String.class);

        var resp = scrapperRestClient()
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
    @Order(11)
    void scrapper_ClientCanNotReattachLink_returns409() {
        long chatId = 10L;
        String url = "https://github.com/user/repo6";

        scrapperRestClient().post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();

        AddLinkRequest addBody = new AddLinkRequest(url, Collections.emptyList());

        var resp = scrapperRestClient()
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

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> scrapperRestClient()
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
    @Order(12)
    void grpc_scrapper_ClientCanNotReattachLink_throwsStatusRuntime() {
        long chatId = 12L;
        String url = "https://github.com/user/repo7";

        var stub = scrapperGrpcClient();

        backend.academy.linktracker.proto.AddLinkRequest body =
                backend.academy.linktracker.proto.AddLinkRequest.newBuilder()
                        .setChatId(chatId)
                        .setUrl(url)
                        .addTags("tag")
                        .build();

        var add = stub.addLink(body);

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () -> stub.addLink(body));
    }

    @Test
    @Order(13)
    void scrapper_deleteNonExistentChat_returns404() {
        long chatId = 404L;

        HttpClientErrorException.NotFound ex =
                assertThrows(HttpClientErrorException.NotFound.class, () -> scrapperRestClient()
                        .delete()
                        .uri("/tg-chat/{id}", chatId)
                        .retrieve()
                        .toBodilessEntity());

        assertEquals(ex.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(14)
    void grpc_scrapper_deleteNonExistentChat_throwsStatusRuntime() {
        long chatId = 404L;

        var stub = scrapperGrpcClient();

        RemoveChatRequest request =
                RemoveChatRequest.newBuilder().setChatId(chatId).build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () -> stub.removeChat(request));
    }
}
