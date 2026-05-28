package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.model.ProcessedMessage;
import java.util.Optional;

public interface ProcessedMessagesRepository {

    Optional<ProcessedMessage> findProcessedMessageById(Long id);

    void save(ProcessedMessage processedMessage);
}
