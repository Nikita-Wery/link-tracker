package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.scheduler.old-event")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class FailOldEventsSchedulerProperties {

    @Min(2)
    private int intervalTimeoutMin;
}
