package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.Link;
import java.util.Optional;

public interface ChatRepository {

    Chat save(Chat chat);

    void deleteById(long chatId);

    Optional<Chat> findChatById(long chatId);

    Link addLinkToChat(Link link, long chatId);

    void deleteLinkFromChat(Link link, long chatId);

}
