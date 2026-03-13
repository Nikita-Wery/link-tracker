package backend.academy.linktracker.bot.exception.scrapperexception.responsexception;

import backend.academy.linktracker.bot.exception.ScrapperApiException;

public class UnknownScrapperApiException extends ScrapperApiException {
    public UnknownScrapperApiException(String message) {
        super(message);
    }
}
