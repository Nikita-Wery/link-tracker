package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.client.scheduler")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class SchedulerProperties {

    @Min(1)
    private int threadPoolSize;

    @Min(3000)
    private long intervalUpdateMs;

    @Min(1)
    private int batchSize;

    @Min(1)
    private int queueCapacity;
}
