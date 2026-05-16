package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.scheduler.outbox")
public class OutboxSchedulerProperties {

    @Min(2)
    private int batchSize;

    @Min(3000)
    private long intervalUpdateMs;
}
