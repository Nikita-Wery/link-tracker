package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import java.util.Optional;

public interface ChatLinkRepository {

    ChatLink save(ChatLink chatLink);

    Optional<ChatLink> findChatLinkById(ChatLink.Id id);

    ChatLink deleteChatLink(ChatLink chatLink);
}
