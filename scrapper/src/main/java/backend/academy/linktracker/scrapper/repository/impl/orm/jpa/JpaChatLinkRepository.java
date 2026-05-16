package backend.academy.linktracker.scrapper.repository.impl.orm.jpa;

import backend.academy.linktracker.scrapper.domain.ChatLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaChatLinkRepository extends JpaRepository<ChatLink, Long> {

    @Query("""
        SELECT cl FROM ChatLink cl
        LEFT JOIN FETCH cl.tags
        WHERE cl.chat.chatId = :chatId
            AND cl.link.linkId = (
                SELECT l.linkId
                FROM Link l
                WHERE l.url = :linkUrl
            )
    """)
    Optional<ChatLink> findChatLinkWithTagsInitializeOnly(@Param("chatId") long chatId, @Param("linkUrl") String url);

    @Query("""
        SELECT cl FROM ChatLink cl
        JOIN FETCH cl.link
        WHERE cl.chat.chatId = :chatChatId
    """)
    List<ChatLink> findLinkByChatChatId(long chatChatId);

    @Query("""
        SELECT DISTINCT cl FROM ChatLink cl
        JOIN FETCH cl.tags
        WHERE cl.link.linkId IN :ids
    """)
    List<ChatLink> findChatIdsThatTrackLinks(@Param("ids") List<Long> linkIds);
}
