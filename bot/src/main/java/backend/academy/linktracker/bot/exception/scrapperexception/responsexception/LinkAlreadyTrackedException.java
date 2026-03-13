package backend.academy.linktracker.bot.exception.scrapperexception.responsexception;

import backend.academy.linktracker.bot.exception.ScrapperApiException;

public class LinkAlreadyTrackedException extends ScrapperApiException {
    public LinkAlreadyTrackedException(String message) {
        super(message);
    }
}
