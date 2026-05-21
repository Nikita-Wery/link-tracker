package backend.academy.linktracker.scrapper.config.cache;

import backend.academy.linktracker.scrapper.repository.cache.ChatLinkLocalCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

@Slf4j
@RequiredArgsConstructor
class ChatLinksInvalidateListener implements MessageListener {

    private final ChatLinkLocalCache localCache;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {

            String body = new String(message.getBody());

            log.info("Received invalidation for chatId: {}", body);

            Long chatId = Long.parseLong(body);

            localCache.evict(chatId);

        } catch (NumberFormatException e) {
            log.error("Failed to parse chatId from message", e);
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
