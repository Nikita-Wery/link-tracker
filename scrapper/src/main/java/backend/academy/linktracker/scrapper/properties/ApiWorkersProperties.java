package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.client.api-workers")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ApiWorkersProperties {

    @Min(1)
    private int threadPoolSize;

    @Min(1)
    private int queueCapacity;
}
