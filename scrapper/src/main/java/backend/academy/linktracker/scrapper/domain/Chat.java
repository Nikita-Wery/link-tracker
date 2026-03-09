package backend.academy.linktracker.scrapper.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Chat {

    private long chatId;

    // TODO: подумать позже над Map и PK
    private Set<Link> trackedLinks;

    public Chat(long chatId) {
        this.chatId = chatId;
        this.trackedLinks = new HashSet<>();
    }

    public boolean addLink(Link link) {
        return trackedLinks.add(link);
    }

    public void untrackLink(Link link) {
        Iterator<Link> iterator = trackedLinks.iterator();

        while (iterator.hasNext()) {
            Link nowLink = iterator.next();

            // TODO: подумать над equals по бизнесс ключу
            if (nowLink.equals(link)) {
                iterator.remove();
            }
        }
    }

}
