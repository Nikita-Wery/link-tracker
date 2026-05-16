package backend.academy.linktracker.bot.configuration.kafka;

import backend.academy.linktracker.bot.dto.LinkUpdate;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

@Configuration
@RequiredArgsConstructor
@EnableKafka
@ConditionalOnProperty(name = "app.client.scrapper.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "json", matchIfMissing = true)
public class JsonKafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;
    public static final String DEFAULT_GROUP_ID = "bot-notification-service";
    public static final int DEFAULT_CONCURRENCY = 3;

    @Bean("jsonConsumerFactory")
    public ConcurrentKafkaListenerContainerFactory<Long, LinkUpdate> defaultConsumerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<Long, LinkUpdate>();

        factory.setConsumerFactory(consumerFactory(
                new JacksonJsonDeserializer<>(LinkUpdate.class),
                props -> props.put(ConsumerConfig.GROUP_ID_CONFIG, DEFAULT_GROUP_ID)));

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        // TODO: add custom error handler
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setAutoStartup(true);
        factory.setConcurrency(DEFAULT_CONCURRENCY);
        return factory;
    }

    @Bean
    public KafkaTemplate<Long, LinkUpdate> dlqJsonLinkUpdateKafkaTemplate() {
        var props = kafkaProperties.buildProducerProperties();

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        var factory = new DefaultKafkaProducerFactory<Long, LinkUpdate>(props);
        return new KafkaTemplate<>(factory);
    }

    private <M> ConsumerFactory<Long, M> consumerFactory(
            Deserializer<M> valueDeserializer, Consumer<Map<String, Object>> propsModifier) {

        var props = kafkaProperties.buildConsumerProperties();

        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());

        propsModifier.accept(props);

        return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), valueDeserializer);
    }
}
