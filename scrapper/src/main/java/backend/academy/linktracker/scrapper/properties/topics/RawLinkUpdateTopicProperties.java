package backend.academy.linktracker.scrapper.properties.topics;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.kafka.topics.link-raw-updates")
public class RawLinkUpdateTopicProperties {

    @NotBlank
    private String name;

    @Min(1)
    private int partitions;

    @Min(1)
    private short replicas;
}
