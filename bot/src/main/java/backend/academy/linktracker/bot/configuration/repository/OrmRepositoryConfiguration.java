package backend.academy.linktracker.bot.configuration.repository;

import backend.academy.linktracker.bot.repository.ProcessedMessagesRepository;
import backend.academy.linktracker.bot.repository.impl.orm.OrmProcessedMessagesRepository;
import backend.academy.linktracker.bot.repository.impl.orm.jpa.JpaProcessedMessagesRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.db.access-type", havingValue = "orm", matchIfMissing = true)
public class OrmRepositoryConfiguration {

    @Bean
    public ProcessedMessagesRepository ormProcessedMessagesRepository(
            JpaProcessedMessagesRepository jpaProcessedMessagesRepository) {
        return new OrmProcessedMessagesRepository(jpaProcessedMessagesRepository);
    }
}
