package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.TelegramMessageSender;
import backend.academy.linktracker.bot.application.command.AbstractCommand;
import backend.academy.linktracker.bot.client.ScrapperClient;
import backend.academy.linktracker.bot.dto.LinkResponse;
import backend.academy.linktracker.bot.dto.ListLinksResponse;
import backend.academy.linktracker.bot.exception.scrapperexception.responsexception.ChatNotExistsException;
import backend.academy.linktracker.bot.utils.validator.TagsValidator;
import com.pengrad.telegrambot.model.Update;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListCommand extends AbstractCommand<Update> {

    public static final String COMMAND_NAME = "/list";
    public static final String COMMAND_DESCRIPTION = "Вывести список всех ссылок, отслеживаемых пользователем";
    public static final String ILLEGAL_TEG_MESSAGE = "Тэги введённые вами не могут существовать";

    private static final String NEVER_ATTACHED_LINK_MESSAGE =
            "Чтобы получить список отслеживаемых ссылок, начните отслеживать хотя бы одну";
    private static final String NO_TRACKED_LINKS_MESSAGE = "Сейчас вы не отслеживаете ни одной ссылки";
    private static final Pattern wordSeparators = Pattern.compile("[,\\s]+");

    private final ScrapperClient client;
    private final TelegramMessageSender sender;
    private final TagsValidator tagsValidator;

    public ListCommand(ScrapperClient client, TelegramMessageSender sender, TagsValidator tagsValidator) {

        super(COMMAND_NAME, COMMAND_DESCRIPTION);
        this.client = client;
        this.sender = sender;
        this.tagsValidator = tagsValidator;
    }

    @Override
    public void handle(Update update) {
        Long chatId = update.message().chat().id();
        String[] tags = Arrays.copyOfRange(
                update.message().text().split(wordSeparators.pattern()),
                1,
                update.message().text().split(wordSeparators.pattern()).length);

        try {

            ListLinksResponse listLinksResponse = client.getLinks(chatId);

            List<LinkResponse> linkResponses = listLinksResponse.links();

            if (listLinksResponse.size() == 0) {
                sender.sendMessage(update.message().chat().id(), NO_TRACKED_LINKS_MESSAGE);
                return;
            }

            if (Arrays.stream(tags).allMatch(tagsValidator::validate)) {

                linkResponses = getFilteredLinkResponseListByTags(linkResponses, tags);
                String infoAboutLinks = buildLinkCommandText(linkResponses);

                sender.sendMessage(chatId, infoAboutLinks);

            } else {
                sender.sendMessage(chatId, ILLEGAL_TEG_MESSAGE);
            }

        } catch (ChatNotExistsException ex) {
            log.info("The user tried to get chats, but he didn't attach any", ex);
            sender.sendMessage(update.message().chat().id(), NEVER_ATTACHED_LINK_MESSAGE);
        }
    }

    private String buildLinkCommandText(List<LinkResponse> linkResponses) {

        return "Активные подписки:\n\n"
                + linkResponses.stream()
                        .map(link -> {
                            String tags = link.tags().isEmpty() ? "нет" : String.join(", ", link.tags());
                            return link.url() + ", тэги: " + tags;
                        })
                        .collect(Collectors.joining("\n"));
    }

    private List<LinkResponse> getFilteredLinkResponseListByTags(List<LinkResponse> allResponses, String[] tags) {

        if (tags.length == 0) {
            return allResponses;
        }

        Set<String> distinctTags = new HashSet<>(Arrays.asList(tags));

        return allResponses.stream()
                .filter(response -> response.tags().stream().anyMatch(distinctTags::contains))
                .toList();
    }
}
