package backend.academy.linktracker.scrapper.exception.botexception.requestexception;

import backend.academy.linktracker.scrapper.exception.botexception.ScrapperApiException;

public class InvalidLinkInRequestException extends ScrapperApiException {

    private static final String INVALID_LINK_EXCEPTION = "В данный момент эта ссылка не поддерживается";

    public InvalidLinkInRequestException(String message) {
        super(message, INVALID_LINK_EXCEPTION);
    }
}
