package backend.academy.linktracker.scrapper.containers.kafka;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

public final class RegistryContainerFactory {

    private static final String DEFAULT_IMAGE_NAME = "confluentinc/cp-schema-registry:7.7.8";
    private static final String DEFAULT_PROTOCOL = "PLAINTEXT";

    public static GenericContainer<?> create(
        int port,
        Network network,
        String aliases,
        String hostName,
        String registryListeners,
        String bootstrapServers
    ) {

        return new GenericContainer<>(
            DockerImageName.parse(DEFAULT_IMAGE_NAME)
        )
            .withExposedPorts(port)
            .withNetwork(network)
            .withNetworkAliases(aliases)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", hostName)
            .withEnv("SCHEMA_REGISTRY_LISTENERS", registryListeners)
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", bootstrapServers)
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_SECURITY_PROTOCOL", DEFAULT_PROTOCOL);
    }
}
