package backend.academy.linktracker.scrapper.repository.impl.orm.jpa;

import backend.academy.linktracker.scrapper.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaChatRepository extends JpaRepository<Chat, Long> {

    @Modifying
    @Query("""
        DELETE FROM Chat c WHERE c.chatId = :chatId
    """)
    int deleteByChatId(@Param("chatId") long chatId);

}
