package backend.academy.linktracker.scrapper.containers.postgres;

import org.testcontainers.containers.Network;
import org.testcontainers.postgresql.PostgreSQLContainer;

public final class PostgresContainerFactory {

    private static final String DEFAULT_IMAGE_NAME = "postgres:18-alpine";

    private PostgresContainerFactory() {}

    public static PostgreSQLContainer create(
            String dbName,
            Network network,
            String alias
    ) {

        return new PostgreSQLContainer(DEFAULT_IMAGE_NAME)
                        .withDatabaseName(dbName)
                        .withUsername("test")
                        .withPassword("test")
                        .withNetwork(network)
                        .withNetworkAliases(alias);
    }
}
