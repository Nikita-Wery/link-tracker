package backend.academy.linktracker.ai.configuration.kafka.consumers;

import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
// TODO:
@ConditionalOnProperty(name = "app.client.scrapper.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
public class AvroKafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    // TODO: изменить
    public static final String DEFAULT_GROUP_ID = "bot-notification-service";
    public static final int DEFAULT_CONCURRENCY = 3;

    @Bean("avroConsumerFactory")
    public ConcurrentKafkaListenerContainerFactory<Long, RawLinkUpdateEvent> defaultConsumerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<Long, RawLinkUpdateEvent>();

        factory.setConsumerFactory(consumerFactory(linkUpdateAvroDeserializer(), props -> {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, DEFAULT_GROUP_ID);
            props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        }));

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setAutoStartup(true);
        factory.setConcurrency(DEFAULT_CONCURRENCY);
        return factory;
    }

    private <M> ConsumerFactory<Long, M> consumerFactory(
        Deserializer<M> valueDeserializer, Consumer<Map<String, Object>> propsModifier) {

        var props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());

        propsModifier.accept(props);

        return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), valueDeserializer);
    }

    @SuppressWarnings("Обеспечиваем типобезопасность")
    private Deserializer<RawLinkUpdateEvent> linkUpdateAvroDeserializer() {
        return (Deserializer<RawLinkUpdateEvent>) (Deserializer<?>) new KafkaAvroDeserializer();
    }
}
