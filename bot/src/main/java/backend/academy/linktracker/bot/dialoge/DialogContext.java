package backend.academy.linktracker.bot.dialoge;

import lombok.Getter;
import lombok.Setter;
import java.net.URI;
import java.util.Set;

@Getter
@Setter
public class DialogContext {

    private DialogState state;
    private URI url;
    private Set<String> tags;
    private Set<String> filters;

    public DialogContext(DialogState firstState) {
        this.state = firstState;
    }
}
