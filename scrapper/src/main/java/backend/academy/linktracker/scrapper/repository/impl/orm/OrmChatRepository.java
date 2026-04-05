package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class OrmChatRepository implements ChatRepository {

    private final JpaChatRepository jpaChatRepository;

    @Override
    public boolean existsById(long chatId) {
        return jpaChatRepository.existsById(chatId);
    }

    @Override
    public Chat save(Chat chat) {
        return jpaChatRepository.save(chat);
    }

    @Override
    public int deleteByChatId(long chatId) {
        return jpaChatRepository.deleteByChatId(chatId);
    }

    @Override
    public Optional<Chat> findChatByChatId(long chatId) {
        return jpaChatRepository.findById(chatId);
    }
}
