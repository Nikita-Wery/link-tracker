package backend.academy.linktracker.scrapper.integrations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.configuration.TestCacheConfiguration;
import backend.academy.linktracker.scrapper.configuration.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.cache.ChatLinkLocalCache;
import backend.academy.linktracker.scrapper.repository.cache.ChatLinkRedisCache;
import backend.academy.linktracker.scrapper.service.subscriptionimpl.CacheSubscriptionService;
import backend.academy.linktracker.scrapper.service.subscriptionimpl.SubscriptionServiceBase;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Slf4j
@Tag("integration")
@SpringBootTest()
@Import({TestCacheConfiguration.class, TestcontainersConfiguration.class})
@ActiveProfiles("test-cache")
@Testcontainers
public class RedisIntegrationTest {

    @MockitoBean
    SubscriptionServiceBase subscriptionService;

    @MockitoSpyBean
    ChatLinkLocalCache chatLinkLocalCache;

    @MockitoSpyBean
    ChatLinkRedisCache getChatLinkRedisCache;

    @Autowired
    CacheSubscriptionService cacheSubscriptionService;

    @Autowired
    ChatLinkRedisCache chatLinkRedisCache;

    @Container
    static GenericContainer<?> valkey =
            new GenericContainer<>("valkey/valkey:7.2").withExposedPorts(6379).waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", valkey::getHost);
        registry.add("spring.data.redis.port", valkey::getFirstMappedPort);
    }

    @Test
    void getLinkResponses_butNoOneLinkFoundInCache_ShouldSaveLinksInRedisAndLocalCache() {

        Link link = new Link("https://github.com/linux/repo", ResourceType.GITHUB_REPOSITORY, OffsetDateTime.now());
        link.setLinkId(888L);

        Chat chat = new Chat(888);

        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");

        ChatLink chatLink = new ChatLink(link, chat, tags);
        chatLink.setChatLinkId(888L);

        List<ChatLink> chatLinks = new ArrayList<>();
        chatLinks.add(chatLink);

        when(subscriptionService.getTrackedLinksByChatId(anyLong())).thenReturn(chatLinks);

        cacheSubscriptionService.getLinkResponsesByChatId(chat.getChatId());

        assertEquals(1, chatLinkRedisCache.getAll(888L).size());
        assertEquals(chatLinkRedisCache.getAll(888L).getFirst().id(), 888L);

        assertEquals(1, chatLinkLocalCache.get(888L).get().links().size());
        assertEquals(chatLinkLocalCache.get(888L).get().links().getFirst().id(), 888L);
    }

    @Test
    void trackNewLink_shouldInvokeEvictLocalCache_andSaveInRedis() {

        Link link = new Link("https://github.com/linux/repo", ResourceType.GITHUB_REPOSITORY, OffsetDateTime.now());
        link.setLinkId(777L);

        Chat chat = new Chat(777);

        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");

        ChatLink chatLink = new ChatLink(link, chat, tags);
        chatLink.setChatId(777L);
        chatLink.setLinkId(777L);
        chatLink.setChatLinkId(777L);

        when(subscriptionService.trackLink(any())).thenReturn(chatLink);

        cacheSubscriptionService.trackLink(chatLink);

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

            verify(chatLinkLocalCache).evict(captor.capture());

            assertEquals(777L, captor.getValue());
        });

        assertEquals(1, chatLinkRedisCache.getAll(777L).size());
        assertEquals(chatLinkRedisCache.getAll(777L).getFirst().id(), 777L);
    }

    @Test
    void untrackLink_shouldInvokeEvictLocalCache_andInvokeEvictInRedis() {

        Link link = new Link("https://github.com/linux/repo", ResourceType.GITHUB_REPOSITORY, OffsetDateTime.now());
        link.setLinkId(333L);

        Chat chat = new Chat(222L);

        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");

        ChatLink chatLink = new ChatLink(link, chat, tags);
        chatLink.setChatId(222L);
        chatLink.setLinkId(333L);
        chatLink.setChatLinkId(444L);

        when(subscriptionService.untrackLink(any())).thenReturn(chatLink);

        cacheSubscriptionService.untrackLink(chatLink);

        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ArgumentCaptor<Long> captorLocal = ArgumentCaptor.forClass(Long.class);

            verify(chatLinkLocalCache).evict(captorLocal.capture());

            assertEquals(222L, captorLocal.getValue());
        });

        ArgumentCaptor<Long> captorRedis = ArgumentCaptor.forClass(Long.class);

        verify(chatLinkRedisCache).evict(captorRedis.capture(), captorRedis.capture());

        assertEquals(222L, captorRedis.getAllValues().get(0));
        assertEquals(444L, captorRedis.getAllValues().get(1));
    }
}
