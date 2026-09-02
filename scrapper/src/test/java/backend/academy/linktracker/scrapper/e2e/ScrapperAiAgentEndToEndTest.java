package backend.academy.linktracker.scrapper.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.TestPropertySource;
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

@TestPropertySource(properties = {"app.client.bot.api.kafka.enabled=true"})
@Tag("integration")
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScrapperAiAgentEndToEndTest {

    static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.5.2");
    static final DockerImageName SCHEMA_REGISTRY = DockerImageName.parse("confluentinc/cp-schema-registry:7.7.8");
    static final DockerImageName LIQUIBASE_IMAGE = DockerImageName.parse("liquibase/liquibase:latest-alpine");
    static final DockerImageName SCRAPPER_IMAGE = DockerImageName.parse("scrapper:0.0.1");
    static final DockerImageName AI_AGENT_IMAGE = DockerImageName.parse("ai-agent:0.0.1");

    static Network network = Network.newNetwork();

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
            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
            .withStartupCheckStrategy(new OneShotStartupCheckStrategy())
            .dependsOn(scrapperPostgres);

    @Container
    GenericContainer<?> scrapper = new GenericContainer<>(SCRAPPER_IMAGE)
            .withExposedPorts(8081, 9091)
            .withEnv("MANAGEMENT_HEALTH_REDIS_ENABLED", "false")
            .withEnv("SPRING_TASK_SCHEDULING_ENABLED", "true")
            .withEnv("APP_SCHEDULER_ENABLED", "true")
            .withEnv("APP_SCHEDULER_LINK_UPDATE_ENABLED", "false")
            .withEnv("APP_CACHE_ENABLED", "false")
            .withEnv("APP_CLIENT_BOT_API_REST_ENABLED", "true")
            .withEnv("APP_CLIENT_BOT_API_GRPC_ENABLED", "true")
            .withEnv("APP_CLIENT_BOT_API_KAFKA_ENABLED", "true")
            .withEnv("SPRING_PROFILES_ACTIVE", "kafka")
            .withEnv("SPRING_KAFKA_PRODUCER_PROPERTIES_SCHEMA_REGISTRY_URL", "http://schema-registry:8087")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
            .withEnv("APP_SCHEDULER_OUTBOX_INTERVAL_UPDATE_MS", "3001")
            .withEnv("SPRING_KAFKA_PRODUCER_PROPERTIES_LINGER_MS", "1000")
            .withEnv("APP_KAFKA_TOPICS_LINK_RAW_UPDATES_REPLICAS", "1")
            .withEnv("APP_KAFKA_TOPICS_LINK_UPDATES_REPLICAS", "1")
            .withEnv("APP_KAFKA_TOPICS_LINK_RAW_UPDATES_PARTITIONS", "3")
            .withEnv("APP_KAFKA_SERIALIZATION", "avro")
            .withEnv("APP_DB_ACCESS_TYPE", "orm")
            .withEnv("DB_HOST", "scrapper-postgres")
            .withEnv("DB_PORT", "5432")
            .withEnv("DB_NAME", "linktracker_db")
            .withEnv("DB_USERNAME", "test")
            .withEnv("DB_PASSWORD", "test")
            .withEnv("SPRING_LIQUIBASE_ENABLED", "false")
            .withNetwork(network)
            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
            .withNetworkAliases("scrapper")
            .dependsOn(schemaRegistry, scrapperLiquibase)
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8081));

    @Container
    GenericContainer<?> aiAgent = new GenericContainer<>(AI_AGENT_IMAGE)
            .withExposedPorts(8085)
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_SCHEMA_REGISTRY_URL", "http://schema-registry:8087")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_FETCH_MIN_BYTES", "2")
            .withEnv("APP_KAFKA_TOPICS_LINK_PROCESSED_UPDATES_REPLICAS", "1")
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_MAX_POLL_INTERVAL_MS", "3000")
            .withNetwork(network)
            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
            .withNetworkAliases("ai-agent")
            .dependsOn(scrapper, schemaRegistry)
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8085));

    @Test
    void ai_consumer_shouldContainMessage() {

        ConsumerRecord<Long, Object> record = pollMessage("link-raw-updates");

        assertNotNull(record);

        Object value = record.value();

        assertInstanceOf(RawLinkUpdateEvent.class, value);

        RawLinkUpdateEvent event = (RawLinkUpdateEvent) value;

        log.info("TEST CONSUMER RECEIVED key={}, value={}", record.key(), event);

        assertEquals(999L, event.getId());
    }

    private ConsumerRecord<Long, Object> pollMessage(String topic) {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + UUID.randomUUID());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        props.put(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8087));

        KafkaAvroDeserializer deserializer = new KafkaAvroDeserializer();

        deserializer.configure(props, false);

        try (KafkaConsumer<Long, Object> consumer = new KafkaConsumer<>(props, new LongDeserializer(), deserializer)) {

            consumer.subscribe(List.of(topic));

            ConsumerRecords<Long, Object> records = consumer.poll(Duration.ofSeconds(10));

            assertFalse(records.isEmpty());

            return records.records(topic).iterator().next();
        }
    }
}
