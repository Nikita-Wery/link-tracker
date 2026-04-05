package backend.academy.linktracker.scrapper.repository.impl.orm.jpa;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface JpaChatLinkRepository extends JpaRepository<ChatLink, Long> {

    @Query("""
        SELECT cl FROM ChatLink cl
        LEFT JOIN FETCH cl.tags
        WHERE cl.chat.chatId = :chatId AND cl.link.url = :linkUrl
    """)
    Optional<ChatLink> findChatLinkWithTagsInitializeOnly(@Param("chatId") long chatId, @Param("linkUrl") String url);

    List<ChatLink> findChatLinkByChatChatId(long chatChatId);

}
