package backend.academy.linktracker.bot.repository.impl.orm;

import backend.academy.linktracker.bot.domain.ProcessedMessage;
import backend.academy.linktracker.bot.repository.ProcessedMessagesRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;

@AllArgsConstructor
public class OrmProcessedMessagesRepository implements ProcessedMessagesRepository {

    private JpaRepository<ProcessedMessage, Long> jpaRepository;

    @Override
    public Optional<ProcessedMessage> findProcessedMessageById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void save(ProcessedMessage processedMessage) {
        jpaRepository.save(processedMessage);
    }
}
