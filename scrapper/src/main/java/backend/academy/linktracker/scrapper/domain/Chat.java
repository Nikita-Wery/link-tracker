package backend.academy.linktracker.scrapper.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Chat {

    private Long id;

    @NotNull
    private final Long chatId;

    @Builder.Default
    private Set<ChatLink> trackedLinks = new HashSet<>();

    public boolean addLink(ChatLink chatLink) {
        return trackedLinks.add(chatLink);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o != null & o instanceof Chat) {
            Chat that = (Chat) o;
            return this.chatId == that.chatId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return chatId.hashCode();
    }

}
