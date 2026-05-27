package backend.academy.linktracker.ai.configuration.kafka.producers;

import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
// TODO:
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;
    public static final int DEFAULT_DLQ_ACKS = 1;

    @Bean
    @ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
    public KafkaTemplate<Long, ProcessedLinkUpdate> avroKafkaTemplate() {

        var props = kafkaProperties.buildProducerProperties();

        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        var factory = new DefaultKafkaProducerFactory<Long, ProcessedLinkUpdate>(props);
        return new KafkaTemplate<>(factory);
    }


    @Bean
    public KafkaTemplate<Long, RawLinkUpdateEvent> dlqAvroLinkUpdateKafkaTemplate() {

        var props = kafkaProperties.buildProducerProperties();

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, DEFAULT_DLQ_ACKS);

        var factory = new DefaultKafkaProducerFactory<Long, RawLinkUpdateEvent>(props);
        return new KafkaTemplate<>(factory);
    }
}
