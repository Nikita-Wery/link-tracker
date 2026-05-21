package backend.academy.linktracker.scrapper.client.inner.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@RequiredArgsConstructor
public class ChatLinksEventPublisher {

    private static final String INVALIDATE_CHANNEL_TOPIC_NAME = "chat_link:invalidate";
    private final StringRedisTemplate redisTemplate;

    public void publishInvalidate(Long chatId) {

        redisTemplate.convertAndSend(INVALIDATE_CHANNEL_TOPIC_NAME, String.valueOf(chatId));
        log.info("New chatLinkInvalidEvent published for chatId: {}", chatId);
    }
}
