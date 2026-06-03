package backend.academy.linktracker.bot.dialog.trackdialog.actions;

import static net.logstash.logback.argument.StructuredArguments.kv;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.impl.TrackCommand;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dialog.DialogContext;
import backend.academy.linktracker.bot.dialog.StateAction;
import backend.academy.linktracker.bot.dto.AddLinkRequest;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.InvalidLinkInRequestException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.LinkAlreadyTrackedException;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ScrapperServerException;
import backend.academy.linktracker.bot.logging.aspect.BotMetricsService;
import backend.academy.linktracker.bot.repository.DialogContextStorage;
import backend.academy.linktracker.bot.utils.validator.TagsValidator;
import com.pengrad.telegrambot.model.Update;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.micrometer.core.instrument.Timer;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TagsAction implements StateAction {

    private static final String DEFAULT_SUCCESS_MESSAGE = "Ссылка сохранена";
    private static final String INVALID_LINK_MESSAGE =
            "Извините, но ссылка, которую вы пытаетесь прикрепить, перестала поддерживаться";
    private static final String LINK_ALREADY_TRACKED_MESSAGE =
            "Ссылка, которую вы пытаетесь прикрепить, уже отслеживается";
    private static final String SCRAPPER_SERVER_ERROR_MESSAGE = "Извините, произошла непредвиденная ошибка";
    private static final int ALLOWED_NUMBER_OF_TAGS = 10;
    private static final String TOO_MANY_REQUESTS_MESSAGE = "Слишком много запросов. Попробуйте позже.";
    private static final String SERVICE_UNAVAILABLE_MESSAGE = "Сервис временно недоступен.";

    private final ScrapperClient client;
    private final DialogContextStorage storage;
    private final TelegramMessageSender sender;
    private final TagsValidator tagsValidator;
    private final BotMetricsService botMetricsService;

    public TagsAction(
            ScrapperClient client,
            DialogContextStorage storage,
            TelegramMessageSender sender,
            TagsValidator tagsValidator,
            BotMetricsService botMetricsService) {

        this.client = client;
        this.storage = storage;
        this.sender = sender;
        this.tagsValidator = tagsValidator;
        this.botMetricsService = botMetricsService;
    }

    @SuppressFBWarnings(
            value = "SLF4J_PLACE_HOLDER_MISMATCH",
            justification = "Используем StructuredArguments для JSON, placeholders не нужны")
    @Override
    public boolean execute(Update update, DialogContext context) {

        String messageText = update.message().text();

        if (tagsValidator.validate(messageText) && messageText.split(",").length <= ALLOWED_NUMBER_OF_TAGS) {

            String[] tagsArray = messageText.split(",", -1);

            Set<String> tags = Arrays.stream(tagsArray).map(String::strip).collect(Collectors.toSet());

            context.setTags(tags);

            AddLinkRequest addLinkRequest = new AddLinkRequest(context.getUrl().toString(), tags);

            try {

                Timer.Sample sample = botMetricsService.startCommandTimer();
                try {
                    client.addLink(update.message().chat().id(), addLinkRequest);
                } finally {
                    botMetricsService.stopCommandTimer(sample, "scrapper_sync_api", "addLink");
                }

                storage.clearDialog(update.message().chat().id());

                sender.sendMessage(update.message().chat().id(), DEFAULT_SUCCESS_MESSAGE);

                return true;
            } catch (InvalidLinkInRequestException ex) {
                log.error("The link was not validated by scrapper", kv("link", context.getUrl()), ex);
                sender.sendMessage(update.message().chat().id(), INVALID_LINK_MESSAGE);
            } catch (LinkAlreadyTrackedException ex) {
                log.info("The user tried to re-attach the link", kv("link", context.getUrl()), ex);
                sender.sendMessage(update.message().chat().id(), LINK_ALREADY_TRACKED_MESSAGE);
            } catch (ScrapperServerException ex) {
                log.error("Scrapper server error", ex);
                sender.sendMessage(update.message().chat().id(), SCRAPPER_SERVER_ERROR_MESSAGE);
            } catch (RequestNotPermitted ex) {
                log.warn("Rate limit exceeded", ex);

                sender.sendMessage(update.message().chat().id(), TOO_MANY_REQUESTS_MESSAGE);
            } catch (CallNotPermittedException ex) {
                log.error("Circuit breaker is open", ex);

                sender.sendMessage(update.message().chat().id(), SERVICE_UNAVAILABLE_MESSAGE);
            } finally {

                botMetricsService.incrementCommand(TrackCommand.COMMAND_NAME);
            }
        }

        return false;
    }
}
