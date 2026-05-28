package backend.academy.linktracker.ai.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka.topics.link-processed-updates")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ProcessedLinkUpdatesTopicProperties {

    @NotBlank
    private String name;

    @Min(1)
    private int partitions;

    @Min(1)
    private short replicas;
}
