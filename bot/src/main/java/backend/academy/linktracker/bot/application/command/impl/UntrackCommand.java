package backend.academy.linktracker.bot.application.command.impl;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.RemoveLinkRequest;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ChatNotExistsException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.LinkNotTrackedException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ScrapperServerException;
import backend.academy.linktracker.bot.utils.validator.LinkValidationProcessor;
import com.pengrad.telegrambot.model.Update;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UntrackCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/untrack";
    public static final String COMMAND_DESCRIPTION = "Прекратить отслеживаение ссылки";

    private static final String MISMATCH_NUMBER_OF_ARGS_MESSAGE =
            "За один раз можно открепить только одну ссылку, ни больше ни меньше";
    private static final String INVALID_LINK_MESSAGE = "Извините, но в данный момент такая ссылка не поддерживается";
    private static final String SUCCESSFUL_LINK_UNPINNING_MESSAGE =
            "Ссылка успешно откреплена и больше не отслеживается";
    private static final String USER_HAS_NEVER_ATTACHED_LINK_MESSAGE =
            "Чтобы открепить ссылку, начните отслеживать хотя бы одну";
    private static final String USER_NOT_CURRENTLY_FOLLOWING_ANY_LINKS_MESSAGE =
            "В данный момент вы не отслеживаете ни одной ссылки";
    private static final String SCRAPPER_SERVER_ERROR_MESSAGE = "Извините, произошла непредвиденная ошибка";
    private static final String TOO_MANY_REQUESTS_MESSAGE = "Слишком много запросов. Попробуйте позже.";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Сервис временно недоступен.";

    private final ScrapperClient client;
    private final TelegramMessageSender sender;
    private final LinkValidationProcessor validator;

    public UntrackCommand(ScrapperClient client, TelegramMessageSender sender, LinkValidationProcessor validator) {

        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.sender = sender;
        this.client = client;
        this.validator = validator;
    }

    @Override
    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    public void handle(Update update) {

        String[] messageWords = update.message().text().split(" ");
        Long chatId = update.message().chat().id();

        if (messageWords.length != 2) {
            sender.sendMessage(update.message().chat().id(), MISMATCH_NUMBER_OF_ARGS_MESSAGE);
            return;
        }

        String link = messageWords[1].trim();

        if (validator.isValid(link)) {

            RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(link);

            try {
                client.untrackLink(chatId, removeLinkRequest);
                sender.sendMessage(chatId, SUCCESSFUL_LINK_UNPINNING_MESSAGE);
            } catch (ChatNotExistsException ex) {
                log.warn("The user has never interacted with the bot before");
                sender.sendMessage(chatId, USER_HAS_NEVER_ATTACHED_LINK_MESSAGE);
            } catch (LinkNotTrackedException ex) {
                log.warn("The user attempted to unpin an untracked link.");
                sender.sendMessage(chatId, USER_NOT_CURRENTLY_FOLLOWING_ANY_LINKS_MESSAGE);
            } catch (ScrapperServerException ex) {
                log.error("Scrapper server error", kv("raw_link", link), ex);
                sender.sendMessage(chatId, SCRAPPER_SERVER_ERROR_MESSAGE);
            } catch (RequestNotPermitted ex) {
                log.warn("Rate limit exceeded", ex);

                sender.sendMessage(update.message().chat().id(), TOO_MANY_REQUESTS_MESSAGE);
            } catch (CallNotPermittedException ex) {
                log.error("Circuit breaker is open", ex);

                sender.sendMessage(update.message().chat().id(), SERVICE_UNAVAILABLE_MESSAGE);
            }

        } else {
            sender.sendMessage(chatId, INVALID_LINK_MESSAGE);
        }
    }
}
