package backend.academy.linktracker.bot.application.dispatcher.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.dispatcher.UpdateDispatcher;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.DialogState;
import backend.academy.linktracker.bot.dialog.AbstractDialogStateMachine;
import backend.academy.linktracker.bot.dialog.DialogStateMachine;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.utils.validator.CommandValidator;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DialogDispatcher implements UpdateDispatcher {

    private static final String DEFAULT_LOST_DIALOG_MESSAGE = "Пожалуйста, выполните действие заново";

    private final DialogContextStorage storage;
    private final CommandValidator commandValidator;
    private final Map<DialogState, AbstractDialogStateMachine<? extends DialogState>>
            dialogStateMachines = new ConcurrentHashMap<>();
    private final TelegramMessageSender sender;

    public DialogDispatcher(
        DialogContextStorage storage,
        CommandValidator commandValidator,
        TelegramMessageSender sender,
        List<? extends AbstractDialogStateMachine<? extends DialogState>> dialogStateMachines
    ) {
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

            Optional<DialogContext> context = storage.findDialogContext(message.chat().id());

            return !message.text().isBlank()
                && context.isPresent()
                && !commandValidator.isCommand(message.text().split(" ")[0].trim());

        }

        return false;
    }

    @Override
    public void dispatch(Update update) {

        Optional<DialogContext> ctx = storage.findDialogContext(update.message().chat().id());

        if (ctx.isPresent()) {
            DialogStateMachine<? extends DialogState> stateMachine = dialogStateMachines.get(ctx.get().getState());
            stateMachine.handle(update, ctx.get());
        } else {
            sender.sendMessage(update.message().chat().id(), DEFAULT_LOST_DIALOG_MESSAGE);
        }

    }
}
