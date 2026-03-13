package backend.academy.linktracker.bot.exception.scrapperexception.responsexception;

import backend.academy.linktracker.bot.exception.ScrapperApiException;

public class InvalidLinkInRequestException extends ScrapperApiException {
    public InvalidLinkInRequestException(String message) {
        super(message);
    }
}
