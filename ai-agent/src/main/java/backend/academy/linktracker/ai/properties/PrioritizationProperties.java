package backend.academy.linktracker.ai.properties;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.prioritization")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class PrioritizationProperties {

    @NotNull
    private List<String> highKeywords;

    @NotNull
    private List<String> lowKeywords;
}
