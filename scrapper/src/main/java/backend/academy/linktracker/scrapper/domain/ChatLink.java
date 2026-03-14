package backend.academy.linktracker.scrapper.domain;

import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ChatLink {

    public static class Id {
        protected Long chatId;

        protected Long linkId;

        public Id(Long linkId, Long chatId) {
            this.linkId = linkId;
            this.chatId = chatId;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o != null && o instanceof Id) {
                Id that = (Id) o;
                return this.chatId.equals(that.chatId) && this.linkId.equals(that.linkId);
            }
            return false;
        }

        public int hashCode() {
            return chatId.hashCode() + linkId.hashCode();
        }
    }

    private Link link;

    private Chat chat;

    private Id id;

    private Set<String> tags = new HashSet<>();

    private Set<String> filters = new HashSet<>();

    @Builder
    public ChatLink(@NotNull Link link, @NotNull Chat chat, Set<String> tags, Set<String> filters) {
        this.link = link;
        this.chat = chat;

        if (tags != null) {
            this.tags = tags;
        }
        if (filters != null) {
            this.filters = filters;
        }

        this.id = new Id(link.getId(), chat.getId());

        link.addChat(this);
        chat.addLink(this);
    }
}
