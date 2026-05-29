package backend.academy.linktracker.scrapper.config.cache;

import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.properties.LocalCacheProperties;
import backend.academy.linktracker.scrapper.properties.RedisCacheProperties;
import backend.academy.linktracker.scrapper.properties.ValkeyClusterProperties;
import backend.academy.linktracker.scrapper.repository.cache.ChatLinkLocalCache;
import backend.academy.linktracker.scrapper.repository.cache.ChatLinkRedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Configuration
@EnableCaching
@Profile("!test-cache")
@ConditionalOnProperty(value = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CacheConfiguration {

    @Bean
    RedisConnectionFactory lettuceConnectionFactory(ValkeyClusterProperties valkeyClusterProperties) {

        valkeyClusterProperties.getCluster().getNodes().forEach(node -> {
            log.info("REDIS NODE: {}", node);
        });

        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(
                valkeyClusterProperties.getCluster().getNodes());

        return new LettuceConnectionFactory(clusterConfig);
    }

    @Bean
    public RedisTemplate<String, LinkResponse> linkRedisTemplate(RedisConnectionFactory factory, ObjectMapper mapper) {

        RedisTemplate<String, LinkResponse> template = new RedisTemplate<>();

        template.setConnectionFactory(factory);

        var jsonSerializer = new GenericJacksonJsonRedisSerializer(mapper);

        template.setKeySerializer(new StringRedisSerializer());

        template.setHashKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public ChatLinkLocalCache chatLinkLocalCache(LocalCacheProperties properties) {
        return new ChatLinkLocalCache(properties);
    }

    @Bean
    public ChatLinkRedisCache chatLinkRedisCache(
            RedisTemplate<String, LinkResponse> template, RedisCacheProperties properties, ObjectMapper objectMapper) {
        return new ChatLinkRedisCache(template, properties, objectMapper);
    }
}
