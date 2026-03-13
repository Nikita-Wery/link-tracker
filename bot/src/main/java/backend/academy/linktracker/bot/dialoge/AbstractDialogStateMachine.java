package backend.academy.linktracker.bot.dialoge;

import java.util.List;

public abstract class AbstractDialogStateMachine<T extends DialogState> implements DialogStateMachine<T> {

    private final List<T> dialogStates;

    public AbstractDialogStateMachine(List<T> startState) {
        this.dialogStates = startState;
    }

}
