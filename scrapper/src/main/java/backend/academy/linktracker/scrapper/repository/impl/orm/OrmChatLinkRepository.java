package backend.academy.linktracker.scrapper.repository.impl.orm;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
import backend.academy.linktracker.scrapper.repository.impl.orm.jpa.JpaChatLinkRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class OrmChatLinkRepository implements ChatLinkRepository {

    private JpaChatLinkRepository jpaChatLinkRepository;

    @Override
    public ChatLink save(ChatLink chatLink) {
        return jpaChatLinkRepository.save(chatLink);
    }

    @Override
    public ChatLink saveAndFlush(ChatLink chatLink) {
        ChatLink savedChatLink = save(chatLink);
        jpaChatLinkRepository.flush();
        return savedChatLink;
    }

    @Override
    public boolean existsById(long id) {
        return jpaChatLinkRepository.existsById(id);
    }

    @Override
    public List<ChatLink> findChatLinksByChatId(long chatId) {
        return jpaChatLinkRepository.findLinkByChatChatId(chatId);
    }

    @Override
    public Optional<ChatLink> deleteChatLinkReturningChatLink(ChatLink chatLink) {
        Optional<ChatLink> result = jpaChatLinkRepository.findChatLinkWithTagsInitializeOnly(
                chatLink.getChat().getChatId(), chatLink.getLink().getUrl());

        result.ifPresent(jpaChatLinkRepository::delete);
        return result;
    }

    @Override
    public List<ChatLink> findChatLinksThatTrackLink(List<Long> linkIds) {
        return jpaChatLinkRepository.findChatIdsThatTrackLinks(linkIds);
    }
}
