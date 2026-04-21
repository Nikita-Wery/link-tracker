package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.db.workers.link-update")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class LinkUpdateWorkerProperties {

    @Min(1)
    private int batchSize;

    @Min(50)
    private long flushTimeout;

    @Min(1)
    private int corePoolSize;

    @Min(1)
    private int maxPoolSize;

    @PositiveOrZero
    private int queueCapacity;
}
