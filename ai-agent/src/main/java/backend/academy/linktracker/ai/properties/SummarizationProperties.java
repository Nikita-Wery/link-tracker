package backend.academy.linktracker.ai.properties;

import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.summarization")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class SummarizationProperties {

    @Min(0)
    private int maxLength;

}
