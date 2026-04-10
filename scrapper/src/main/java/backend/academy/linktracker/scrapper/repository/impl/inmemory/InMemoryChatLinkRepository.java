// package backend.academy.linktracker.scrapper.repository.impl.inmemory;
//
// import backend.academy.linktracker.scrapper.domain.ChatLink;
// import backend.academy.linktracker.scrapper.repository.ChatLinkRepository;
// import java.util.HashSet;
// import java.util.Iterator;
// import java.util.Optional;
// import java.util.Set;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Repository;
//
// @Slf4j
// @Repository
// public class InMemoryChatLinkRepository implements ChatLinkRepository {
//
//    private final Set<ChatLink> chatLinkRepository;
//
//    public InMemoryChatLinkRepository() {
//        this.chatLinkRepository = new HashSet<>();
//    }
//
//    @Override
//    public ChatLink save(ChatLink chatLink) {
//        chatLinkRepository.add(chatLink);
//
//        return chatLink;
//    }
//
//    @Override
//    public Optional<ChatLink> findChatLinkById(ChatLink.BusinessId id) {
//        Optional<ChatLink> result = Optional.empty();
//
//        for (ChatLink chatLink : chatLinkRepository) {
//            if (chatLink.getBusinessId().equals(id)) {
//                return Optional.of(chatLink);
//            }
//        }
//
//        return result;
//    }
//
//    @Override
//    public ChatLink deleteChatLink(ChatLink chatLink) {
//        Iterator<ChatLink> iterator = chatLinkRepository.iterator();
//
//        while (iterator.hasNext()) {
//            if (iterator.next().equals(chatLink)) {
//                ChatLink result = iterator.next();
//                iterator.remove();
//                return result;
//            }
//        }
//
//        return chatLink;
//    }
// }
