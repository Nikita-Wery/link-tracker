package backend.academy.linktracker.bot.dialog;

import java.net.URI;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

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
