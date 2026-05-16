package backend.academy.linktracker.scrapper.config.repositoryconfiguration;

import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.OutboxEventRepository;
import backend.academy.linktracker.scrapper.repository.impl.jdbc.JdbcHelper;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmOutboxEventRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaOutboxEventRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnProperty(name = "app.db.access-type", havingValue = "orm", matchIfMissing = true)
public class OrmRepositoryConfiguration {

    @Bean
    public ChatRepository chatRepository(JpaChatRepository jpaChatRepository) {
        return new OrmChatRepository(jpaChatRepository);
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public LinkRepository linkRepository(JpaLinkRepository linkRepository, JdbcHelper jdbcHelper) {
        return new OrmLinkRepository(linkRepository, jdbcHelper);
    }

    @Bean
    public ChatLinkRepository chatLinkRepository(JpaChatLinkRepository jpaChatLinkRepository) {
        return new OrmChatLinkRepository(jpaChatLinkRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "app.client.bot.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
    public OutboxEventRepository outboxEventRepository(
            JpaOutboxEventRepository jpaOutboxEventRepository, JdbcHelper jdbcHelper) {
        return new OrmOutboxEventRepository(jpaOutboxEventRepository, jdbcHelper);
    }
}
