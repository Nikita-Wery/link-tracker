package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.ai.domain.ProcessedMessage;
import backend.academy.linktracker.ai.repository.ProcessedMessagesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProcessedMessagesService {

    private ProcessedMessagesRepository processedMessagesRepository;

    public Optional<ProcessedMessage> findProcessedMessageById(long id) {
        return processedMessagesRepository.findProcessedMessageById(id);
    }

    public void save(ProcessedMessage processedMessage) {
        processedMessagesRepository.save(processedMessage);
    }
}
