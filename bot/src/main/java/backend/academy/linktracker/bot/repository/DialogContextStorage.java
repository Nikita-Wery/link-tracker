package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.dialoge.DialogContext;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DialogContextStorage {

    private final Map<Long, DialogContext> dialogState = new ConcurrentHashMap<>();

    public void clearDialog(Long chatId) {
        dialogState.remove(chatId);
    }

    public Optional<DialogContext> findDialogContext(Long chatId) {
        return Optional.of(dialogState.get(chatId));
    }

}
