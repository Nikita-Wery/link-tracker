package backend.academy.linktracker.scrapper.utils;

import backend.academy.linktracker.scrapper.config.ResourceType;
import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.domain.ChatLink;
import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.bot.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.bot.ApiErrorResponse;
import backend.academy.linktracker.scrapper.dto.bot.LinkResponse;
import backend.academy.linktracker.scrapper.dto.bot.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.exception.botexception.requestexception.InvalidLinkInRequestException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DtoEntityMapper {

    public Chat getChatFromChatId(Long chatId) {
        return Chat.builder().chatId(chatId).build();
    }

    public LinkResponse linkToLinkResponse(ChatLink link) {
        return new LinkResponse(
                link.getId(), link.getLink().getUrl(), link.getTags().stream().toList());
    }

    public Link linkFromAddLinkRequest(AddLinkRequest request) {
        ResourceType type = getResourceTypeFromURI(request.link());

        return Link.builder().url(URI.create(request.link())).resourceType(type).build();
    }

    public ApiErrorResponse buildApiErrorResponse(String description, String statusCode, Exception ex) {
        return ApiErrorResponse.builder()
                .description(description)
                .code(statusCode)
                .exceptionMessage(ex.getMessage())
                .exceptionName(ex.getClass().getSimpleName())
                .stackTrace(buildStacktrace(ex))
                .build();
    }

    private List<String> buildStacktrace(Exception exception) {
        return Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();
    }

    public Link linkFromRemoveLinkRequest(RemoveLinkRequest request) {
        ResourceType type = getResourceTypeFromURI(request.link());

        return Link.builder().url(URI.create(request.link())).resourceType(type).build();
    }

    private ResourceType getResourceTypeFromURI(String url) {
        return Arrays.stream(ResourceType.values())
                .filter(req -> req.parser().supports(url))
                .findFirst()
                .orElseThrow(() -> new InvalidLinkInRequestException("Unsupported link type " + url));
    }
}
