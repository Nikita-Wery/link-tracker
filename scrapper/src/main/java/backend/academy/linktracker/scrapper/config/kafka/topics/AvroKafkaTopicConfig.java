package backend.academy.linktracker.scrapper.config.kafka;

import backend.academy.linktracker.scrapper.properties.topics.LinkUpdateTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(
    name = "app.kafka.type",
    havingValue = "avro"
)
public class AvroKafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics avroTopics(
        LinkUpdateTopicProperties props
    ) {

        return new KafkaAdmin.NewTopics(
            new NewTopic(
                props.getAvroTopic(),
                props.getPartitions(),
                props.getReplicas()
            )
// TODO: подумать где лучше убарать
//            new NewTopic(
//                props.getAvroTopic() + "-dlt",
//                props.getPartitions(),
//                props.getReplicas()
//            )
        );
    }
}
