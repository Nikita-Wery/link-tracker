package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import java.util.Optional;

public interface ChatRepository {

    boolean existsById(long chatId);

    Chat save(Chat chat);

    int deleteByChatId(long chatId);

    Optional<Chat> findChatByChatId(long chatId);

}
