package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.db.workers.outbox-update")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class OutboxEventUpdateWorkerProperties implements WorkerProperties {

    @Min(8000)
    private int maximumNumberOfUnprocessedElements;

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
