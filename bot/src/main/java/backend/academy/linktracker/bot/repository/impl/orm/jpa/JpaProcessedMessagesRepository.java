package backend.academy.linktracker.bot.repository.impl.orm.jpa;

import backend.academy.linktracker.bot.domain.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProcessedMessagesRepository extends JpaRepository<ProcessedMessage, Long> {}
