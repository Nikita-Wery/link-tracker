package backend.academy.linktracker.scrapper.config.kafka;

import backend.academy.linktracker.contract.avro.LinkUpdateEvent;
import backend.academy.linktracker.contract.avro.RawLinkUpdateEvent;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
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
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    @ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
    public KafkaTemplate<Long, LinkUpdateEvent> avroLinkUpdateKafkaTemplate() {

        var props = kafkaProperties.buildProducerProperties();

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        var factory = new DefaultKafkaProducerFactory<Long, LinkUpdateEvent>(props);
        return new KafkaTemplate<>(factory);
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
    public KafkaTemplate<Long, RawLinkUpdateEvent> avroRawLinkUpdateKafkaTemplate() {

        var props = kafkaProperties.buildProducerProperties();

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        var factory = new DefaultKafkaProducerFactory<Long, RawLinkUpdateEvent>(props);
        return new KafkaTemplate<>(factory);
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "json", matchIfMissing = true)
    public KafkaTemplate<Long, LinkUpdate> jsonLinkUpdateKafkaTemplate() {

        var props = kafkaProperties.buildProducerProperties();

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);

        var factory = new DefaultKafkaProducerFactory<Long, LinkUpdate>(props);
        return new KafkaTemplate<>(factory);
    }
}
