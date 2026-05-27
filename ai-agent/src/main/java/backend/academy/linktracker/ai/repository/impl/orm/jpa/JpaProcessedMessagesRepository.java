package backend.academy.linktracker.ai.repository.impl.orm.jpa;

import backend.academy.linktracker.ai.domain.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProcessedMessagesRepository extends JpaRepository<ProcessedMessage, Long> {
}
