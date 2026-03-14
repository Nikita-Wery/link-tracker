package backend.academy.linktracker.scrapper.domain;

import backend.academy.linktracker.scrapper.utils.IdGenerator;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

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
            return this.chatId.equals(that.chatId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return chatId.hashCode();
    }

    public Long getId() {
        if (this.id == null) {
            this.id = IdGenerator.nextId();
        }

        return id;
    }
}
