package backend.academy.linktracker.bot.dialog.trackdialog.graphs;

import backend.academy.linktracker.bot.dialog.trackdialog.TrackingDialogStates;
import backend.academy.linktracker.bot.dialog.Transition;
import backend.academy.linktracker.bot.dialog.trackdialog.actions.TagsAction;
import backend.academy.linktracker.bot.dialog.trackdialog.actions.UrlAction;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class TrackDialogGraph {

    private final Map<TrackingDialogStates, Transition> transitions;

    public TrackDialogGraph(UrlAction urlAction, TagsAction tagsAction) {

        transitions = Map.of(
            TrackingDialogStates.WAITING_URL,
            new Transition(
                TrackingDialogStates.WAITING_URL,
                TrackingDialogStates.WAITING_TAGS,
                urlAction
            ),
            TrackingDialogStates.WAITING_TAGS,
            new Transition(
                TrackingDialogStates.WAITING_TAGS,
                TrackingDialogStates.DONE,
                tagsAction
            )
        );
    }

    public Transition getTransition(TrackingDialogStates state) {
        return transitions.get(state);
    }

}
