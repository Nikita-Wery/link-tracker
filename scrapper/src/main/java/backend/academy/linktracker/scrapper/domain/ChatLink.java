package backend.academy.linktracker.scrapper.domain;

import backend.academy.linktracker.scrapper.utils.IdGenerator;
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

    public static class BusinessId {
        @Getter
        protected Long chatId;

        @Getter
        protected Long linkId;

        public BusinessId(Long linkId, Long chatId) {
            this.linkId = linkId;
            this.chatId = chatId;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o != null && o instanceof BusinessId) {
                BusinessId that = (BusinessId) o;
                return this.chatId.equals(that.chatId) && this.linkId.equals(that.linkId);
            }
            return false;
        }

        public int hashCode() {
            int result = (chatId == null ? 0 : chatId.hashCode());
            result = 31 * result + (linkId == null ? 0 : linkId.hashCode());
            return result;
        }
    }

    private Long id;

    private Link link;

    private Chat chat;

    private BusinessId businessId;

    private Set<String> tags = new HashSet<>();

    @Builder
    public ChatLink(@NotNull Link link, @NotNull Chat chat, Set<String> tags) {
        this.link = link;
        this.chat = chat;
        this.id = IdGenerator.nextId();

        if (tags != null) {
            this.tags = tags;
        }

        this.businessId = new BusinessId(link.getId(), chat.getId());

        link.addChat(this);
        chat.addLink(this);
    }

    public Long getId() {
        if (this.id == null) {
            this.id = IdGenerator.nextId();
        }

        return id;
    }
}
