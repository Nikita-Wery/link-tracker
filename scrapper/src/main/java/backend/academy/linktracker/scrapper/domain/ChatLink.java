package backend.academy.linktracker.scrapper.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.Id;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "chat_link",
    uniqueConstraints = @UniqueConstraint(
        name = "unq_chatid_linkurl",
        columnNames = {"chat_id", "link_url"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class ChatLink {

    @Id
    @Column(name = "chat_link_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_link_seq")
    @SequenceGenerator(
        name = "chat_link_seq",
        sequenceName = "chat_link_sequence"
    )
    private Long chatLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(
        name = "link_url",
        referencedColumnName = "url",
        updatable = false,
        nullable = false
    )
    private Link link;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(
        name = "chat_id",
        referencedColumnName = "chat_id",
        updatable = false,
        nullable = false
    )
    private Chat chat;

    @ElementCollection
    @CollectionTable(
        name = "chat_link_tags",
        joinColumns = @JoinColumn(name = "chat_link_id")
    )
    @Column(name = "tag", nullable = false)
    private Set<String> tags = new HashSet<>();

    public ChatLink(@NotNull Link link, @NotNull Chat chat) {
        this.link = link;
        this.chat = chat;

        link.addChat(this);
        chat.addLink(this);
    }

    public ChatLink(@NotNull Link link, @NotNull Chat chat, Set<String> tags) {
        this(link, chat);

        if (tags != null) {
            this.tags = tags;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatLink)) return false;
        ChatLink that = (ChatLink) o;
        return chat.equals(that.chat) && link.equals(that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chat, link);
    }

}
