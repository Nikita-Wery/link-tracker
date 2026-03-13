package backend.academy.linktracker.bot.exception.scrapperexception.responsexception;

import backend.academy.linktracker.bot.exception.ScrapperApiException;

public class ChatNotExistsException extends ScrapperApiException {
    public ChatNotExistsException(String message) {
        super(message);
    }
}
