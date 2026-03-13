package backend.academy.linktracker.scrapper.exception.botexception.responsexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class InvalidLinkUpdateException extends BotApiException {

    public InvalidLinkUpdateException(String message) {
        super(message);
    }
}
