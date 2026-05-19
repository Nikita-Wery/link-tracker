package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.scheduler.link-update")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class LinkUpdateSchedulerProperties {

    @Min(50)
    private int batchSize;

    @Min(500)
    private long intervalUpdateMs;
}
