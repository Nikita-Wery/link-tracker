package backend.academy.linktracker.scrapper.config.repositoryconfiguration;

import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcLinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "app.db.access-type", havingValue = "sql")
public class JdbcRepositoryConfiguration {

    @Bean
    public ChatRepository chatRepository(JdbcClient jdbcClient) {
        return new JdbcChatRepository(jdbcClient);
    }

    @Bean
    LinkRepository linkRepository(JdbcClient jdbcClient) {
        return new JdbcLinkRepository(jdbcClient);
    }

    @Bean
    ChatLinkRepository chatLinkRepository(JdbcClient jdbcClient, DataSource dataSource) {
    return new JdbcChatLinkRepository(jdbcClient, dataSource);
    }

}
