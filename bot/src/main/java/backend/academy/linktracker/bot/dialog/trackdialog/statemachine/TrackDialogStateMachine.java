package backend.academy.linktracker.bot.dialog.trackdialog.statemachine;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.bot.application.command.impl.TrackCommand;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.DialogStateMachine;
import backend.academy.linktracker.bot.dialog.Transition;
import backend.academy.linktracker.bot.dialog.trackdialog.TrackingDialogStates;
import backend.academy.linktracker.bot.dialog.trackdialog.graphs.TrackDialogGraph;
import com.pengrad.telegrambot.model.Update;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrackDialogStateMachine implements DialogStateMachine<TrackingDialogStates> {

    private final TrackDialogGraph dialogGraph;

    public TrackDialogStateMachine(TrackDialogGraph dialogGraph) {
        this.dialogGraph = dialogGraph;
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
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
