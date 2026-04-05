package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaChatLinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@AllArgsConstructor
public class OrmChatLinkRepository implements ChatLinkRepository {

    private JpaChatLinkRepository jpaChatLinkRepository;

    @Override
    public ChatLink save(ChatLink chatLink) {
        return jpaChatLinkRepository.save(chatLink);
    }

    @Override
    public boolean existsById(long id) {
        return jpaChatLinkRepository.existsById(id);
    }

    @Override
    public List<ChatLink> findChatLinksByChatId(long chatId) {
        return jpaChatLinkRepository.findChatLinkByChatChatId(chatId);
    }

    @Override
    public Optional<ChatLink> deleteChatLinkReturningChatLink(ChatLink chatLink) {
        Optional<ChatLink> result = jpaChatLinkRepository.findChatLinkWithTagsInitializeOnly(chatLink.getChat().getChatId(), chatLink.getLink().getUrl());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        jpaChatLinkRepository.delete(result.get());

        return result;
    }
}
