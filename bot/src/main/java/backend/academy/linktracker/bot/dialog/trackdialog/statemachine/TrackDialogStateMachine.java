package backend.academy.linktracker.bot.dialog.trackdialog.statemachine;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.bot.application.command.impl.TrackCommand;
import backend.academy.linktracker.bot.dialog.AbstractDialogStateMachine;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.Transition;
import backend.academy.linktracker.bot.dialog.trackdialog.TrackingDialogStates;
import backend.academy.linktracker.bot.dialog.trackdialog.graphs.TrackDialogGraph;
import com.pengrad.telegrambot.model.Update;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrackDialogStateMachine extends AbstractDialogStateMachine<TrackingDialogStates> {

    private final TrackDialogGraph dialogGraph;

    public TrackDialogStateMachine(TrackDialogGraph dialogGraph) {
        super(Arrays.stream(TrackingDialogStates.values()).toList());
        this.dialogGraph = dialogGraph;
    }

    @Override
    public void handle(Update update, DialogContext context) {

        Transition transition = dialogGraph.getTransition((TrackingDialogStates) context.getState());

        if (transition != null) {

            if (transition.action().execute(update, context)) {
                context.setState(transition.to());
            }

        } else {
            log.error(
                    "Failed to perform transition in {} dialog",
                    TrackCommand.COMMAND_NAME,
                    kv("chat_id", update.message().chat().id()),
                    kv("dialog_state", context.getState()));
        }
    }

    @Override
    public List<TrackingDialogStates> getAllStates() {
        return Arrays.stream(TrackingDialogStates.values()).toList();
    }
}
