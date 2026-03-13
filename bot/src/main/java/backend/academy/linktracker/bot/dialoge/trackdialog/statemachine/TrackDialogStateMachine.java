package backend.academy.linktracker.bot.dialoge.trackdialog.statemachine;

import backend.academy.linktracker.bot.dialoge.AbstractDialogStateMachine;
import backend.academy.linktracker.bot.dialoge.DialogContext;
import backend.academy.linktracker.bot.dialoge.Transition;
import backend.academy.linktracker.bot.dialoge.trackdialog.TrackingDialogStates;
import backend.academy.linktracker.bot.dialoge.trackdialog.graphs.TrackDialogGraph;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class TrackDialogStateMachine extends AbstractDialogStateMachine<TrackingDialogStates> {

    private final TrackDialogGraph dialogGraph;

    public TrackDialogStateMachine(TrackDialogGraph dialogGraph) {
        super(Arrays.stream(TrackingDialogStates.values()).toList());
        this.dialogGraph = dialogGraph;
    }

    @Override
    public void handle(Update update, DialogContext context) {
 //        Optional<DialogContext> ctx = storage.findDialogContext(update.message().chat().id());
//
//        if (ctx.isEmpty()) {
//            telegramMessageSender.sendMessage(update.message().chat().id(), DEFAULT_LOST_DIALOG_MESSAGE);
//        }
//
//        if (ctx.get().getState() == TrackingDialogStates.IDLE) {
//            ctx.get().setState(TrackingDialogStates.WAITING_URL);
//            telegramMessageSender.sendMessage(update.message().chat().id(), "Send link");
//            return;
//        }
//
        Transition transition = dialogGraph.getTransition((TrackingDialogStates) context.getState());
//
        if (transition != null) {

            transition.action().execute(update, context);

            context.setState(transition.to());
        }
    }

    @Override
    public List<TrackingDialogStates> getAllStates() {
        return Arrays.stream(TrackingDialogStates.values()).toList();
    }
}
