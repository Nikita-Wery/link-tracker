package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.Set;

public record LinkUpdate(
        @NotNull(message = "link id can not be null") Long id,

        @NotNull(message = "link url can not be blank") URI url,

        String description,

        @NotEmpty(message = "chats that follow the link can not be empty")
        Set<@NotNull Long> tgChatIds) {}
