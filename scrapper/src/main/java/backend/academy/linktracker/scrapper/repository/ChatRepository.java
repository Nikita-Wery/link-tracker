package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Chat;
import java.util.Optional;

public interface ChatRepository {

    boolean existsById(long chatId);

    Chat save(Chat chat);

    Chat saveAndFlush(Chat chat);

    int deleteByChatId(long chatId);

    Optional<Chat> findChatByChatId(long chatId);
}
