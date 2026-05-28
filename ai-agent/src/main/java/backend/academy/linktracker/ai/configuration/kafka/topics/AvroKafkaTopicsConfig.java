package backend.academy.linktracker.ai.configuration.kafka.topics;

import backend.academy.linktracker.ai.properties.ProcessedLinkUpdatesTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class AvroKafkaTopicsConfig {

    @Bean
    public KafkaAdmin.NewTopics avroTopics(ProcessedLinkUpdatesTopicProperties props) {
        return new KafkaAdmin.NewTopics(new NewTopic(props.getName(), props.getPartitions(), props.getReplicas()));
    }
}
