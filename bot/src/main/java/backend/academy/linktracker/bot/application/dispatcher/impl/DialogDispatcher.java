package backend.academy.linktracker.bot.application.dispatcher.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.dispatcher.UpdateDispatcher;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.DialogState;
import backend.academy.linktracker.bot.dialog.DialogStateMachine;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.utils.validator.CommandValidator;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class DialogDispatcher implements UpdateDispatcher {

    private static final String DEFAULT_LOST_DIALOG_MESSAGE = "Пожалуйста, выполните действие заново";

    private final DialogContextStorage storage;
    private final CommandValidator commandValidator;
    private final Map<DialogState, DialogStateMachine<? extends DialogState>> dialogStateMachines =
            new ConcurrentHashMap<>();
    private final TelegramMessageSender sender;

    public DialogDispatcher(
            DialogContextStorage storage,
            CommandValidator commandValidator,
            TelegramMessageSender sender,
            List<? extends DialogStateMachine<? extends DialogState>> dialogStateMachines) {
        dialogStateMachines.forEach(dialogStateMachine -> {
            dialogStateMachine.getAllStates().forEach(dialogState -> {
                this.dialogStateMachines.put(dialogState, dialogStateMachine);
            });
        });
        this.sender = sender;
        this.commandValidator = commandValidator;
        this.storage = storage;
    }

    @Override
    public boolean supports(Update update) {
        Message message = update.message();

        if (message != null) {

            Optional<DialogContext> context =
                    storage.findDialogContext(message.chat().id());

            return !message.text().isBlank()
                    && context.isPresent()
                    && !commandValidator.isCommand(message.text().split(" ")[0].trim());
        }

        return false;
    }

    @Override
    public void dispatch(Update update) {

        storage.findDialogContext(update.message().chat().id())
                .ifPresentOrElse(
                        ctx -> {
                            DialogStateMachine<? extends DialogState> stateMachine =
                                    dialogStateMachines.get(ctx.getState());
                            stateMachine.handle(update, ctx);
                        },
                        () -> sender.sendMessage(update.message().chat().id(), DEFAULT_LOST_DIALOG_MESSAGE));
    }
}
