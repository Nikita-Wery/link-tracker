package backend.academy.linktracker.scrapper.properties.topics;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties("app.kafka.link-updates")
@Configuration
public class LinkUpdateTopicProperties {

    private String jsonTopic;
    private String avroTopic;
    private int partitions;
    private short replicas;

}
