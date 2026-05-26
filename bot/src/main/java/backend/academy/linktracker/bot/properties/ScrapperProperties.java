package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.client.scrapper")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Component
public class ScrapperProperties {

    @NotEmpty
    private String host;

    @NotEmpty
    private String grpcHost;

    @NotNull
    private Timeout timeout;

    @NotNull
    private Api api;

    @Getter
    @Setter
    public static class Timeout {

        @NotNull
        private Duration connection;

        @NotNull
        private Duration read;
    }

    @Getter
    @Setter
    public static class Api {

        private Transport kafka;

        private Transport rest;

        private Transport grpc;
    }

    @Getter
    @Setter
    public static class Transport {
        private boolean enabled;
    }
}
