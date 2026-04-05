package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChatLinkRepository {

    ChatLink save(ChatLink chatLink);

    boolean existsById(long id);

    List<ChatLink> findChatLinksByChatId(long chatId);

    Optional<ChatLink> deleteChatLinkReturningChatLink(ChatLink chatLink);
}
