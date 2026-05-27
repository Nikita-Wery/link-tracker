package backend.academy.linktracker.ai.configuration.kafka.producers;

import backend.academy.linktracker.ai.client.KafkaBotClient;
import backend.academy.linktracker.ai.model.ProcessedLinkUpdate;
import backend.academy.linktracker.ai.properties.RawLinkUpdatesTopicProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class KafkaClientConfig {

    private final RawLinkUpdatesTopicProperties props;

    @Bean
    //TODO: изменить в соответствии с yaml
    @ConditionalOnProperty(name = "app.clint.transport", havingValue = "kafka")
    public KafkaBotClient kafkaBotClient(
            KafkaTemplate<Long, ProcessedLinkUpdate> kafkaTemplate) {

        return new KafkaBotClient(props, kafkaTemplate);
    }

}
