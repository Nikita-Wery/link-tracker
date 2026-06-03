package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.proxy")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ProxyProperties {

    @Min(1000)
    private int port;

    @NotNull
    private String host;

    @NotNull
    private String pass;

    @NotNull
    private String userName;
}
