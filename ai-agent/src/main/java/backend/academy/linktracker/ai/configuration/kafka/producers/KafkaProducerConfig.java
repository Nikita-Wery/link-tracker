package backend.academy.linktracker.ai.configuration.kafka.producers;

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import backend.academy.linktracker.contract.avro.ProcessedLinkUpdateEvent;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;
    public static final String DEFAULT_DLQ_ACKS = "all";

    @Bean(name = "avroKafkaTemplate")
    public KafkaTemplate<Long, ProcessedLinkUpdateEvent> processedLinkUpdateEventKafkaTemplate() {

        var props = kafkaProperties.buildProducerProperties();

        props.put(KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        var factory = new DefaultKafkaProducerFactory<Long, ProcessedLinkUpdateEvent>(props);
        return new KafkaTemplate<>(factory);
    }

    @Bean
    public KafkaTemplate<Long, RawLinkUpdateEvent> dlqAvroRawLinkUpdateKafkaTemplate() {

        var props = kafkaProperties.buildProducerProperties();

        props.put(KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put(ACKS_CONFIG, DEFAULT_DLQ_ACKS);

        var factory = new DefaultKafkaProducerFactory<Long, RawLinkUpdateEvent>(props);
        return new KafkaTemplate<>(factory);
    }
}
