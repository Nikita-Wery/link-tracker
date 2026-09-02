package backend.academy.linktracker.scrapper.integrations;

import backend.academy.linktracker.scrapper.config.repositoryconfiguration.OrmRepositoryConfiguration;
import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(OrmRepositoryConfiguration.class)
@Testcontainers
public class DatabaseIntegrationTest {

    static Network network = Network.newNetwork();

    @Autowired
    ChatRepository chatRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:18-alpine")
        .withDatabaseName("linktracker_db")
        .withUsername("test")
        .withPassword("test")
        .withNetwork(network)
        .withNetworkAliases("scrapper-postgres");

    @Container
    static GenericContainer<?> liquibaseContainer = new GenericContainer<>("liquibase/liquibase:latest-alpine")
        .withNetwork(network)
        .withFileSystemBind(
            Path.of("")
                .toAbsolutePath()
                .getParent()
                .resolve("migrations/migrations-k6-test")
                .toString(),
            "/liquibase/changelog",
            BindMode.READ_ONLY)
//        .withFileSystemBind("../migrations/migrations-scrapper", "/liquibase/scrapper", BindMode.READ_ONLY)
        .withCommand(
            "--url=jdbc:postgresql://scrapper-postgres:5432/linktracker_db",
            "--username=test",
            "--password=test",
            "--changeLogFile=changelog-root.yaml",
            "update")
        .withLogConsumer(frame -> System.out.print(frame.getUtf8String()))
        .withStartupCheckStrategy(new OneShotStartupCheckStrategy())
        .dependsOn(postgreSQLContainer);

    @Test
    public void findChatInDatabase() {

        Chat chat = new Chat(1L);

        assertEquals(chat, chatRepository.findChatByChatId(1L));

    }

}
