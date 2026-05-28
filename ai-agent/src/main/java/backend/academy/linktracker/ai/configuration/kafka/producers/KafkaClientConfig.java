package backend.academy.linktracker.ai.configuration.kafka.producers;

import backend.academy.linktracker.ai.client.KafkaBotClient;
import backend.academy.linktracker.ai.properties.ProcessedLinkUpdatesTopicProperties;
import backend.academy.linktracker.ai.utils.mappers.AvroMapper;
import backend.academy.linktracker.contract.avro.ProcessedLinkUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class KafkaClientConfig {

    private final ProcessedLinkUpdatesTopicProperties props;

    @Bean
    public KafkaBotClient kafkaBotClient(
            @Qualifier("avroKafkaTemplate") KafkaTemplate<Long, ProcessedLinkUpdateEvent> kafkaTemplate,
            AvroMapper avroMapper) {

        return new KafkaBotClient(props, kafkaTemplate, avroMapper);
    }
}
