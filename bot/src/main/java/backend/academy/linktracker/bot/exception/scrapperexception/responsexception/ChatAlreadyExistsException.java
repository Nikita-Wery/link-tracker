package backend.academy.linktracker.bot.exception.scrapperexception.responsexception;

import backend.academy.linktracker.bot.exception.ScrapperApiException;

public class ChatAlreadyExistsException extends ScrapperApiException {
    public ChatAlreadyExistsException(String message) {
        super(message);
    }
}
