package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.model.ProcessedMessage;
import backend.academy.linktracker.bot.repository.ProcessedMessagesRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
