package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkUpdate(
        @NotNull(message = "link id can not be null") Long id,

        @NotNull(message = "link url can not be blank") String url,

        String description,

        @NotEmpty(message = "chats that follow the link can not be empty")
        List<@NotNull Long> tgChatIds) {}
