package backend.academy.linktracker.scrapper.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.controller.rest.LinkController;
import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.ChatService;
import backend.academy.linktracker.scrapper.service.SubscriptionService;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LinkController.class)
@Import(LinkControllerTest.TestConfig.class)
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private DtoEntityMapper dtoEntityMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class TestConfig {

        @Bean
        public SubscriptionService subscriptionService() {
            return mock(SubscriptionService.class);
        }

        @Bean
        public DtoEntityMapper dtoEntityMapper() {
            return mock(DtoEntityMapper.class);
        }

        @Bean
        public ChatService chatService() {
            return mock(ChatService.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(subscriptionService, dtoEntityMapper);
    }

    @Test
    @DisplayName("GET /links: успешное получение списка ссылок")
    void getLinks_Success() throws Exception {
        Long chatId = 1L;

        List<LinkResponse> responses = List.of(new LinkResponse(1L, "https://example.com", List.of()));

        given(subscriptionService.getLinkResponsesByChatId(chatId)).willReturn(responses);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.links[0].url").value("https://example.com"));
    }

    @Test
    @DisplayName("POST /links: успешное добавление ссылки")
    void trackLink_Success() throws Exception {
        Long chatId = 1L;

        AddLinkRequest request = new AddLinkRequest("https://github.com/torvalds/repo1", List.of("tag1"));

        Link link = new Link("https://github.com/torvalds/repo1", mock(ResourceType.class), OffsetDateTime.now());
        Chat chat = new Chat(chatId);
        ChatLink chatLink = new ChatLink(link, chat, new HashSet<>(request.tags()));

        LinkResponse response = new LinkResponse(1L, "https://github.com/torvalds/repo1", List.of("tag1"));

        given(dtoEntityMapper.linkFromAddLinkRequest(any())).willReturn(link);
        given(dtoEntityMapper.getChatFromChatId(chatId)).willReturn(chat);
        given(subscriptionService.trackLink(any())).willReturn(chatLink);
        given(subscriptionService.trackLinkReturnLinkResponse(any())).willReturn(response);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://github.com/torvalds/repo1"))
                .andExpect(jsonPath("$.tags[0]").value("tag1"));
    }

    @Test
    @DisplayName("DELETE /links: успешное удаление ссылки")
    void untrackLink_Success() throws Exception {
        Long chatId = 1L;

        RemoveLinkRequest request = new RemoveLinkRequest("https://github.com/torvalds/repo1");

        Link link = new Link("https://github.com/torvalds/repo1", mock(ResourceType.class), OffsetDateTime.now());
        Chat chat = new Chat(chatId);
        ChatLink chatLink = new ChatLink(link, chat);

        LinkResponse response = new LinkResponse(1L, "https://github.com/torvalds/repo1", List.of());

        given(dtoEntityMapper.linkFromRemoveLinkRequest(any())).willReturn(link);
        given(dtoEntityMapper.getChatFromChatId(chatId)).willReturn(chat);
        given(subscriptionService.untrackLink(any())).willReturn(chatLink);
        given(subscriptionService.untrackLinkReturnLinkResponse(any())).willReturn(response);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://github.com/torvalds/repo1"));
    }

    @Test
    @DisplayName("POST /links: невалидный body")
    void trackLink_InvalidBody() throws Exception {
        mockMvc.perform(post(URI.create("/links"))
                        .header("Tg-Chat-Id", 1L)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
