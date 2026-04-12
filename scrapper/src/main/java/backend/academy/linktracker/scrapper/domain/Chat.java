package backend.academy.linktracker.scrapper.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chats")
@Getter
@Setter
@NoArgsConstructor
public class Chat {

    @Id
    @Column(name = "chat_id")
    private long chatId;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.REMOVE)
    private Set<ChatLink> trackedLinks = new HashSet<>();

    public Chat(long chatId) {
        this.chatId = chatId;
    }

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
        return Objects.hashCode(chatId);
    }
}
