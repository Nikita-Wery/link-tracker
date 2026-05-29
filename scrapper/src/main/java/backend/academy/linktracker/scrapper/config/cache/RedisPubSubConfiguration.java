package backend.academy.linktracker.scrapper.config.cache;

import backend.academy.linktracker.scrapper.client.inner.redis.ChatLinksEventPublisher;
import backend.academy.linktracker.scrapper.repository.cache.ChatLinkLocalCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Configuration
@ConditionalOnProperty(value = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RedisPubSubConfiguration {

    private static final String INVALIDATE_CHANNEL_TOPIC_NAME = "chat_link:invalidate";

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory, ChatLinksInvalidateListener listener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(listener, new ChannelTopic(INVALIDATE_CHANNEL_TOPIC_NAME));

        container.setErrorHandler(e -> {
            log.error("Error in Redis message listener", e);
        });

        container.setAutoStartup(true);

        return container;
    }

    @Bean
    public ChatLinksInvalidateListener chatLinksInvalidateListener(ChatLinkLocalCache cache) {
        return new ChatLinksInvalidateListener(cache);
    }

    @Bean
    public ChatLinksEventPublisher chatLinksEventPublisher(StringRedisTemplate template) {
        return new ChatLinksEventPublisher(template);
    }
}
