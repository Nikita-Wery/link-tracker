package backend.academy.linktracker.bot.exception.scrapperexception.responsexception;

import backend.academy.linktracker.bot.exception.ScrapperApiException;

public class ScrapperServerException extends ScrapperApiException {
    public ScrapperServerException(String message) {
        super(message);
    }
}
