package backend.academy.linktracker.scrapper.containers.liquibase;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class LiquibaseContainerFactory {

    public static GenericContainer<?> create(
            PostgreSQLContainer postgres,
            String postgresContainerAlias,
            String migrationPath,
            Network network
    ) {

        return new GenericContainer<>(
                DockerImageName.parse(
                        "liquibase/liquibase:latest-alpine"
                )
        )
        .withNetwork(network)
        .withFileSystemBind(
                migrationPath,
                "/liquibase/changelog",
                BindMode.READ_ONLY
        )
        .withCommand(
                "--url=jdbc:postgresql://%s:5432/%s"
                        .formatted(
                                postgresContainerAlias,
                                postgres.getDatabaseName()
                        ),
                "--username=test",
                "--password=test",
                "--changeLogFile=changelog-root.yaml",
                "update"
        );
    }
}
