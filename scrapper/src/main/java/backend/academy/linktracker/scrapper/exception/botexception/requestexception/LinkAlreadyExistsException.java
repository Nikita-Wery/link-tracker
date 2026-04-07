package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;

public class LinkAlreadyExistsException extends ScrapperApiException {

    private static final String LINK_ALREADY_EXISTS_EXCEPTION =
            "Такая ссылка уже была добавлена неким пользователем ранее";

    public LinkAlreadyExistsException(String message) {
        super(message, LINK_ALREADY_EXISTS_EXCEPTION);
    }
}
