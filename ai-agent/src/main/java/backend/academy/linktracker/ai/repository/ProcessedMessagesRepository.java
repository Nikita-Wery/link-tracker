package backend.academy.linktracker.ai.repository;

import backend.academy.linktracker.ai.domain.ProcessedMessage;
import java.util.Optional;

public interface ProcessedMessagesRepository {

    Optional<ProcessedMessage> findProcessedMessageById(Long id);

    void save(ProcessedMessage processedMessage);
}
