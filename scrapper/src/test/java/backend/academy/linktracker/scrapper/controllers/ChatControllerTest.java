package backend.academy.linktracker.scrapper.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.controller.rest.ChatController;
import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.dto.bot.ApiErrorResponse;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.ChatNotExistsException;
import backend.academy.linktracker.scrapper.exception.handler.RestExceptionHandler;
import backend.academy.linktracker.scrapper.service.ChatService;
import backend.academy.linktracker.scrapper.utils.DtoEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
@Import({ChatControllerTest.TestConfig.class, RestExceptionHandler.class})
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatService chatService;

    @Autowired
    private DtoEntityMapper dtoEntityMapper;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ChatService chatService() {
            return mock(ChatService.class);
        }

        @Bean
        public DtoEntityMapper dtoEntityMapper() {
            return mock(DtoEntityMapper.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(chatService, dtoEntityMapper);

        given(dtoEntityMapper.buildApiErrorResponse(any(), any(), any()))
                .willAnswer(invocation -> ApiErrorResponse.builder()
                        .description(invocation.getArgument(0))
                        .code(invocation.getArgument(1))
                        .exceptionName(invocation.getArgument(2).getClass().getSimpleName())
                        .build());
    }

    @Test
    @DisplayName("POST /tg-chat/{id}: успешная регистрация")
    void registerChat_Success() throws Exception {
        Long chatId = 123L;

        Chat chat = new Chat(chatId);

        given(dtoEntityMapper.getChatFromChatId(chatId)).willReturn(chat);
        given(chatService.addChat(chat)).willReturn(chat); // если метод НЕ void

        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /tg-chat/{id}: чат уже существует")
    void registerChat_AlreadyExists() throws Exception {
        Long chatId = 123L;

        Chat chat = new Chat(chatId);

        given(dtoEntityMapper.getChatFromChatId(chatId)).willReturn(chat);
        given(chatService.addChat(chat)).willThrow(new ChatAlreadyExistsException("Chat exists"));

        mockMvc.perform(post("/tg-chat/{id}", chatId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("409"))
                .andExpect(jsonPath("$.exceptionName").value("ChatAlreadyExistsException"));
    }

    @Test
    @DisplayName("DELETE /tg-chat/{id}: успешное удаление")
    void deleteChat_Success() throws Exception {
        Long chatId = 123L;

        willDoNothing().given(chatService).deleteChatById(chatId);

        mockMvc.perform(delete("/tg-chat/{id}", chatId)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /tg-chat/{id}: чат не найден")
    void deleteChat_NotFound() throws Exception {
        Long chatId = 999L;

        willThrow(new ChatNotExistsException("Not found")).given(chatService).deleteChatById(chatId);

        mockMvc.perform(delete("/tg-chat/{id}", chatId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.exceptionName").value("ChatNotExistsException"));
    }
}
