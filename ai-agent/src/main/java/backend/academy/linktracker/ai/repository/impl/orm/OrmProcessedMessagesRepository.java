package backend.academy.linktracker.ai.repository.impl.orm;

import backend.academy.linktracker.ai.domain.ProcessedMessage;
import backend.academy.linktracker.ai.repository.ProcessedMessagesRepository;
import backend.academy.linktracker.ai.repository.impl.orm.jpa.JpaProcessedMessagesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class OrmProcessedMessagesRepository implements ProcessedMessagesRepository {

    private JpaProcessedMessagesRepository jpaRepository;

    @Override
    public Optional<ProcessedMessage> findProcessedMessageById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void save(ProcessedMessage processedMessage) {
        jpaRepository.save(processedMessage);
    }
}
