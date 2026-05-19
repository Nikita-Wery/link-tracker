package backend.academy.linktracker.scrapper.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.kafka.support.serializer.JacksonJsonDeserializer.TRUSTED_PACKAGES;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("integration")
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BotScrapperKafkaEndToEndTest {

    static final DockerImageName BOT_IMAGE = DockerImageName.parse("bot:0.0.1");
    static final DockerImageName SCRAPPER_IMAGE = DockerImageName.parse("scrapper:0.0.1");
    static final DockerImageName LIQUIBASE_IMAGE = DockerImageName.parse("liquibase/liquibase:latest-alpine");
    static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.5.2");
    static final DockerImageName SCHEMA_REGISTRY = DockerImageName.parse("confluentinc/cp-schema-registry:7.7.8");

    static Network network = Network.newNetwork();

    static JdbcTemplate botJdbc;

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(KAFKA_IMAGE)
            .withNetwork(network)
            .withListener("kafka:29092")
            .withNetworkAliases("kafka");

    @Container
    static PostgreSQLContainer scrapperPostgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("linktracker_db")
            .withUsername("test")
            .withPassword("test")
            .withNetwork(network)
            .withNetworkAliases("scrapper-postgres");

    @Container
    static PostgreSQLContainer botPostgres = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("bot_db")
            .withUsername("test")
            .withPassword("test")
            .withNetwork(network)
            .withNetworkAliases("bot-postgres");

    @Container
    static GenericContainer<?> schemaRegistry = new GenericContainer<>(SCHEMA_REGISTRY)
            .withExposedPorts(8087)
            .withNetwork(network)
            .withNetworkAliases("schema-registry")
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8087")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "kafka:29092")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", "PLAINTEXT")
            .dependsOn(kafka)
            .waitingFor(Wait.forListeningPort())
            .waitingFor(Wait.forHttp("/subjects"))
            .withStartupTimeout(Duration.ofSeconds(120));

    @Container
    static GenericContainer<?> scrapperLiquibase = new GenericContainer<>(LIQUIBASE_IMAGE)
            .withNetwork(network)
            .withFileSystemBind(
                    Path.of("")
                            .toAbsolutePath()
                            .getParent()
                            .resolve("migrations/migrations-scrapper-kafka-test")
                            .toString(),
                    "/liquibase/changelog",
                    BindMode.READ_ONLY)
            .withFileSystemBind("../migrations/migrations-scrapper", "/liquibase/scrapper", BindMode.READ_ONLY)
            .withCommand(
                    "--url=jdbc:postgresql://scrapper-postgres:5432/linktracker_db",
                    "--username=test",
                    "--password=test",
                    "--changeLogFile=changelog-root.yaml",
                    "update")
            //            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
            .withStartupCheckStrategy(new OneShotStartupCheckStrategy())
            .dependsOn(scrapperPostgres);

    @Container
    static GenericContainer<?> botLiquibase = new GenericContainer<>(LIQUIBASE_IMAGE)
            .withNetwork(network)
            .withFileSystemBind(
                    Path.of("")
                            .toAbsolutePath()
                            .getParent()
                            .resolve("migrations/migrations-bot")
                            .toString(),
                    "/liquibase/changelog",
                    BindMode.READ_ONLY)
            .withCommand(
                    "--url=jdbc:postgresql://bot-postgres:5432/bot_db",
                    "--username=test",
                    "--password=test",
                    "--changeLogFile=changelog-root.yaml",
                    "update")
            .dependsOn(botPostgres)
            .withStartupCheckStrategy(new OneShotStartupCheckStrategy());

    @Container
    GenericContainer<?> bot = new GenericContainer<>(BOT_IMAGE)
            .withExposedPorts(8080, 9090)
            .withEnv("APP_LOGGER_FILE_ENABLED", "false")
            .withEnv("APP_TELEGRAM_ENABLED", "false")
            .withEnv("APP_CLIENT_SCRAPPER_API_KAFKA_ENABLED", "true")
            .withEnv("SPRING_PROFILES_ACTIVE", "kafka")
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_SCHEMA_REGISTRY_URL", "http://schema-registry:8087")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_FETCH_MIN_BYTES", "2")
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_MAX_POLL_INTERVAL_MS", "3000")
            .withEnv("APP_KAFKA_SERIALIZATION", "json")
            .withEnv("APP_CLIENT_SCRAPPER_API_GRPC_ENABLED", "true")
            .withEnv("APP_CLIENT_SCRAPPER_API_REST_ENABLED", "true")
            .withEnv("APP_CLIENT_SCRAPPER_HOST", "http://scrapper:8081")
            .withEnv("APP_CLIENT_SCRAPPER_GRPC_HOST", "scrapper:9091")
            .withEnv("DB_HOST", "bot-postgres")
            .withEnv("DB_PORT", "5432")
            .withEnv("DB_NAME", "bot_db")
            .withEnv("DB_USERNAME", "test")
            .withEnv("DB_PASSWORD", "test")
            .withNetwork(network)
            //            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
            .withNetworkAliases("bot")
            .dependsOn(botLiquibase, schemaRegistry)
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8080));

    @Container
    GenericContainer<?> scrapper = new GenericContainer<>(SCRAPPER_IMAGE)
            .withExposedPorts(8081, 9091)
            .withEnv("SPRING_TASK_SCHEDULING_ENABLED", "true")
            .withEnv("APP_CLIENT_BOT_API_REST_ENABLED", "true")
            .withEnv("APP_CLIENT_BOT_API_GRPC_ENABLED", "true")
            .withEnv("APP_CLIENT_BOT_API_KAFKA_ENABLED", "true")
            .withEnv("SPRING_PROFILES_ACTIVE", "kafka")
            .withEnv("SPRING_KAFKA_PRODUCER_PROPERTIES_SCHEMA_REGISTRY_URL", "http://schema-registry:8087")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
            .withEnv("APP_SCHEDULER_OUTBOX_INTERVAL_UPDATE_MS", "3001")
            .withEnv("SPRING_KAFKA_PRODUCER_PROPERTIES_LINGER_MS", "1000")
            .withEnv("APP_KAFKA_TOPICS_LINK_UPDATE_REPLICAS", "1")
            .withEnv("APP_KAFKA_TOPICS_LINK_UPDATE_PARTITIONS", "1")
            .withEnv("APP_KAFKA_SERIALIZATION", "json")
            .withEnv("APP_DB_ACCESS_TYPE", "orm")
            .withEnv("APP_CLIENT_BOT_HOST", "http://bot:8080")
            .withEnv("APP_CLIENT_BOT_GRPC_HOST", "bot:9090")
            .withEnv("DB_HOST", "scrapper-postgres")
            .withEnv("DB_PORT", "5432")
            .withEnv("DB_NAME", "linktracker_db")
            .withEnv("DB_USERNAME", "test")
            .withEnv("DB_PASSWORD", "test")
            .withEnv("SPRING_LIQUIBASE_ENABLED", "false")
            .withNetwork(network)
            //            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
            .withNetworkAliases("scrapper")
            .dependsOn(schemaRegistry, scrapperLiquibase)
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8081));

    @BeforeAll
    void init() {
        botJdbc = new JdbcTemplate(new DriverManagerDataSource(
                botPostgres.getJdbcUrl(), botPostgres.getUsername(), botPostgres.getPassword()));
    }

    private RestClient botRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + bot.getMappedPort(8080))
                .build();
    }

    @Test
    @Order(1)
    void restBotHealthTest() {
        var response = botRestClient()
                .get()
                .uri("/actuator/health/liveness")
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @Order(2)
    void kafka_shouldContainMessage() {

        ConsumerRecord<Long, LinkUpdate> record = pollMessage("link-updates");

        assertNotNull(record);

        log.info("TEST CONSUMER RECEIVED key={}, value={}", record.key(), record.value());
    }

    private ConsumerRecord<Long, LinkUpdate> pollMessage(String topic) {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + UUID.randomUUID());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(TRUSTED_PACKAGES, "*");

        KafkaConsumer<Long, LinkUpdate> consumer = new KafkaConsumer<>(
                props, new LongDeserializer(), new JacksonJsonDeserializer<>(LinkUpdate.class, false));

        consumer.subscribe(List.of(topic));

        ConsumerRecords<Long, LinkUpdate> records = consumer.poll(Duration.ofSeconds(10));

        assertFalse(records.isEmpty());

        return records.records(topic).iterator().next();
    }
}
