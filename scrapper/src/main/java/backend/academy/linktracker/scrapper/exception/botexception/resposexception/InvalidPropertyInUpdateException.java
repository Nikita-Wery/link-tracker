package backend.academy.linktracker.scrapper.exception.botexception.resposexception;

import backend.academy.linktracker.scrapper.exception.botexception.BotApiException;

public class InvalidPropertyInUpdateException extends BotApiException {
    public InvalidPropertyInUpdateException(String message) {
        super(message);
    }
}
