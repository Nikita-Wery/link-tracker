package backend.academy.linktracker.scrapper.config.repositoryconfiguration;

import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcHelper;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcOutboxEventRepository;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnProperty(name = "app.db.access-type", havingValue = "sql")
public class JdbcRepositoryConfiguration {

    @Bean
    public ChatRepository chatRepository(JdbcClient jdbcClient) {
        return new JdbcChatRepository(jdbcClient);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean
    public LinkRepository linkRepository(JdbcClient jdbcClient, JdbcHelper jdbcHelper) {
        return new JdbcLinkRepository(jdbcClient, jdbcHelper);
    }

    @Bean
    public ChatLinkRepository chatLinkRepository(JdbcClient jdbcClient, DataSource dataSource) {
        return new JdbcChatLinkRepository(jdbcClient, dataSource);
    }

    @Bean
    @ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
    public OutboxEventRepository jdbcOutboxEventRepository(
            JdbcClient jdbcClient, JdbcTemplate jdbcTemplate, JdbcHelper jdbcHelper) {
        return new JdbcOutboxEventRepository(jdbcClient, jdbcTemplate, jdbcHelper);
    }
}
