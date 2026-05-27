package backend.academy.linktracker.ai.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.util.List;

// TODO:
@ConfigurationProperties(prefix = "app.filter.")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class FilterProperties {

    @NotEmpty
    private List<String> stopWords;

    @NotEmpty
    private List<String> excludedAuthors;

    @Min(0)
    private int minTextLength;

}
