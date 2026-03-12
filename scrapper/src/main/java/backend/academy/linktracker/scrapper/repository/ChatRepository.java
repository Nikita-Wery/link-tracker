package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import java.util.Optional;

public interface ChatRepository {

    Chat save(Chat chat);

    void deleteByChatId(long chatId);

    Optional<Chat> findChatById(long chatId);

    boolean untrackLink(Chat chat, ChatLink chatLink);
}
