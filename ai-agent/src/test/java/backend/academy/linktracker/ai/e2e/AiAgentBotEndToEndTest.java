package backend.academy.linktracker.ai.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.contract.avro.ProcessedLinkUpdateEvent;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestPropertySource(properties = {"app.client.bot.api.kafka.enabled=true"})
@Tag("integration")
@Slf4j
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AiAgentBotEndToEndTest {

    static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.5.2");
    static final DockerImageName SCHEMA_REGISTRY = DockerImageName.parse("confluentinc/cp-schema-registry:7.7.8");
    static final DockerImageName AI_AGENT_IMAGE = DockerImageName.parse("ai-agent:0.0.1");

    static Network network = Network.newNetwork();

    @Container
    static ConfluentKafkaContainer kafka = new ConfluentKafkaContainer(KAFKA_IMAGE)
            .withNetwork(network)
            .withListener("kafka:29092")
            .withNetworkAliases("kafka");

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
    GenericContainer<?> aiAgent = new GenericContainer<>(AI_AGENT_IMAGE)
            .withExposedPorts(8085)
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_SCHEMA_REGISTRY_URL", "http://schema-registry:8087")
            .withEnv("SPRING_KAFKA_PRODUCER_PROPERTIES_SCHEMA_REGISTRY_URL", "http://schema-registry:8087")
            .withEnv("SPRING_KAFKA_PROPERTIES_SCHEMA_REGISTRY_URL", "http://schema-registry:8087")
            .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:29092")
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_FETCH_MIN_BYTES", "2")
            .withEnv("APP_KAFKA_TOPICS_LINK_PROCESSED_UPDATES_REPLICAS", "1")
            .withEnv("SPRING_KAFKA_CONSUMER_PROPERTIES_MAX_POLL_INTERVAL_MS", "3000")
            .withEnv("APP_GROUPING_WINDOW_MS", "1000")
            .withNetwork(network)
            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
            .withNetworkAliases("ai-agent")
            .dependsOn(schemaRegistry)
            .waitingFor(Wait.forHttp("/actuator/health").forPort(8085));

    @Test
    @SneakyThrows
    void bot_consumer_shouldContainMessage() {

        produceMessage();

        Thread.sleep(6000);

        ConsumerRecord<Long, Object> record = pollMessage("link-processed-updates");

        assertNotNull(record);

        Object value = record.value();

        assertInstanceOf(ProcessedLinkUpdateEvent.class, value);

        ProcessedLinkUpdateEvent event = (ProcessedLinkUpdateEvent) value;

        log.info("TEST CONSUMER RECEIVED key={}, value={}", record.key(), event);

        assertEquals(1L, event.getId());
    }

    private void produceMessage() {

        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);

        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        props.put(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8087));

        ProducerFactory<Long, RawLinkUpdateEvent> factory = new DefaultKafkaProducerFactory<>(props);

        KafkaTemplate<Long, RawLinkUpdateEvent> kafkaTemplate = new KafkaTemplate<>(factory);

        RawLinkUpdateEvent event = RawLinkUpdateEvent.newBuilder()
                .setId(1L)
                .setUrl("https://github.com/torvalds/linux")
                .setAuthor("torvalds")
                .setTgChatIds(List.of())
                .setDescription("Should have HIGH priority security")
                .build();

        kafkaTemplate.send("link-raw-updates", event.getId(), event);

        kafkaTemplate.flush();
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
