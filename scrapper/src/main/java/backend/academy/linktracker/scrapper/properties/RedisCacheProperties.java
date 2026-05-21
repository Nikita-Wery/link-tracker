package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "app.cache.redis")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class RedisCacheProperties {

    @NotNull
    Duration ttl;
}
