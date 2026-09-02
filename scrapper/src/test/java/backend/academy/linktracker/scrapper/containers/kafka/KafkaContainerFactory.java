package backend.academy.linktracker.scrapper.containers.kafka;

import org.testcontainers.containers.Network;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

public final class KafkaContainerFactory {

    private static final String DEFAULT_IMAGE_NAME = "liquibase/liquibase:latest-alpine";

    private KafkaContainerFactory() {}

    public static ConfluentKafkaContainer create(
            String alias,
            Network network,
            String listenerPort
    ) {

        return new ConfluentKafkaContainer(
                DockerImageName.parse(DEFAULT_IMAGE_NAME)
        )
        .withNetwork(network)
        .withListener("kafka:" + listenerPort)
        .withNetworkAliases(alias);
    }
}
