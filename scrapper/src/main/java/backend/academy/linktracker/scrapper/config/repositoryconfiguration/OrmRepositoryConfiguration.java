package backend.academy.linktracker.scrapper.config.repositoryconfiguration;

import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.OrmLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaLinkRepository;
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
    public LinkRepository linkRepository(JpaLinkRepository linkRepository) {
        return new OrmLinkRepository(linkRepository);
    }

    @Bean
    public ChatLinkRepository chatLinkRepository(JpaChatLinkRepository jpaChatLinkRepository) {
        return new OrmChatLinkRepository(jpaChatLinkRepository);
    }
}
