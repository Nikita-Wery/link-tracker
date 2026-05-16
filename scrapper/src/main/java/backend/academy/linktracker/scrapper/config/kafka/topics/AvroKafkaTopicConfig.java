package backend.academy.linktracker.scrapper.config.kafka.topics;

import backend.academy.linktracker.scrapper.properties.topics.LinkUpdateTopicProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.kafka.serialization", havingValue = "avro")
public class AvroKafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics avroTopics(LinkUpdateTopicProperties props) {

        return new KafkaAdmin.NewTopics(
                new NewTopic(props.getName(), props.getPartitions(), props.getReplicas())
                // TODO: подумать где лучше убарать
                //            new NewTopic(
                //                props.getAvroTopic() + "-dlt",
                //                props.getPartitions(),
                //                props.getReplicas()
                //            )
                );
    }
}
