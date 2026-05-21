package backend.academy.linktracker.scrapper.properties;

import java.util.List;
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
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "spring.data.redis")
public class ValkeyClusterProperties {

    private Cluster cluster;
    private String clientType;
    private String host;
    private int port;

    public static class Cluster {
        private List<String> nodes;

        public List<String> getNodes() {
            return nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }
    }
}
