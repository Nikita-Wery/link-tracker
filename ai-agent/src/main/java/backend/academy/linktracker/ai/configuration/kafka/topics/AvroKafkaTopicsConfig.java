package backend.academy.linktracker.ai.configuration.kafka.topics;

import backend.academy.linktracker.ai.properties.RawLinkUpdatesTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
// TODO:
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
public class AvroKafkaTopicsConfig {

    @Bean
    public KafkaAdmin.NewTopics avroTopics(RawLinkUpdatesTopicProperties props) {
        return new KafkaAdmin.NewTopics(new NewTopic(props.getName(), props.getPartitions(), props.getReplicas()));
    }
}
